/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.btree;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import org.openrdf.util.ByteArrayUtil;

/**
 * Implementation of an on-disk B-Tree using the <tt>java.nio</tt> classes that
 * are available in JDK 1.4 and newer. Documentation about B-Trees can be found
 * on-line at the following URLs:
 * <ul>
 * <li>http://cis.stvincent.edu/swd/btree/btree.html</li>,
 * <li>http://bluerwhite.org/btree/</li>, and
 * <li>http://semaphorecorp.com/btp/algo.html</li>.
 * </ul>
 * The first reference was used to implement this class.
 * <p>
 * TODO: clean up code, reuse discarded nodes
 */
public class BTree {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final int FILE_FORMAT_VERSION = 1;
	private static final int HEADER_LENGTH = 16;
	private static final int MRU_CACHE_SIZE = 8;

	/*-----------*
	 * Variables *
	 *-----------*/

	private File _file;
	private RandomAccessFile _raf;
	private FileChannel _fileChannel;

	private BTreeValueComparator _comparator;

	// Stored or specified properties:
	private int _blockSize;
	private int _valueSize;
	private int _rootNodeID;
	private int _maxNodeID;

	// Derived properties:
	private int _slotSize; // The size of a slot storing a node ID and a value
	private int _branchFactor; // The maximum number of outgoing branches for a node
	private int _minValueCount; // The minimum number of values for a node (except for the root)
	private int _nodeSize; // The size of a node in bytes

	/** List containing nodes that are currently "in use". */
	private LinkedList<Node> _nodesInUse;

	/** List containing the most recently used nodes but that are no longer used anymore. */
	private LinkedList<Node> _mruNodes;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new BTree that uses an instance of
	 * <tt>DefaultBTreeValueComparator</tt> to compare values.
	 *
	 * @param dataFile The file for the B-Tree.
	 * @param blockSize The size (in bytes) of a file block for a single node.
	 * Ideally, the size specified is the size of a block in the used file
	 * system.
	 * @param valueSize The size (in bytes) of the fixed-length values that are
	 * or will be stored in the B-Tree.
	 * @throws IOException In case the initialization of the B-Tree file failed.
	 *
	 * @see DefaultBTreeValueComparator
	 */
	public BTree(File dataFile, int blockSize, int valueSize)
		throws IOException
	{
		this(dataFile, blockSize, valueSize, new DefaultBTreeValueComparator());
	}

	/**
	 * Creates a new BTree that uses the supplied <tt>BTreeValueComparator</tt>
	 * to compare the values that are or will be stored in the B-Tree.
	 *
	 * @param dataFile The file for the B-Tree.
	 * @param blockSize The size (in bytes) of a file block for a single node.
	 * Ideally, the size specified is the size of a block in the used file
	 * system.
	 * @param valueSize The size (in bytes) of the fixed-length values that are
	 * or will be stored in the B-Tree.
	 * @param comparator The <tt>BTreeValueComparator</tt> to use for
	 * determining whether one value is smaller, larger or equal to another.
	 * @throws IOException In case the initialization of the B-Tree file failed.
	 */
	public BTree(File dataFile, int blockSize, int valueSize, BTreeValueComparator comparator)
		throws IOException
	{
		if (blockSize < HEADER_LENGTH) {
			throw new IllegalArgumentException("block size must be at least " + HEADER_LENGTH + " bytes");
		}
		if (valueSize <= 0) {
			throw new IllegalArgumentException("value size must be larger than 0");
		}
		if (blockSize < 3 * valueSize + 20) {
			throw new IllegalArgumentException("block size to small; must at least be able to store three values");
		}

		_file = dataFile;
		_comparator = comparator;

		if (!_file.exists()) {
			boolean created = _file.createNewFile();
			if (!created) {
				throw new IOException("Failed to create file: " + _file);
			}
		}

		// Open a read/write channel to the file
		_raf = new RandomAccessFile(_file, "rw");
		_fileChannel = _raf.getChannel();

		if (_fileChannel.size() == 0L) {
			// Empty file, initialize it with the specified parameters
			_blockSize = blockSize;
			_valueSize = valueSize;
			_rootNodeID = 0;
			_maxNodeID = 0;

			_writeFileHeader();
		}
		else {
			// Read parameters from file
			_readFileHeader();
			
			// Verify that the value sizes match
			if (_valueSize != valueSize) {
				throw new IOException(
					"Specified value size (" + valueSize +
					") is different from what is stored on disk (" +
					_valueSize + ")");
			}

			_maxNodeID = _offset2nodeID(_fileChannel.size() - _nodeSize);
		}

		// Calculate derived properties
		_slotSize = 4 + _valueSize;
		_branchFactor = 1 + (_blockSize-8) / _slotSize;
		_minValueCount = (_branchFactor-1) / 2; // bf=30 --> mvc=14; bf=29 --> mvc=14
		_nodeSize = 8 + (_branchFactor-1) * _slotSize;

		_nodesInUse = new LinkedList<Node>();
		_mruNodes = new LinkedList<Node>();

		/*
		System.out.println("blockSize="+_blockSize);
		System.out.println("valueSize="+_valueSize);
		System.out.println("slotSize="+_slotSize);
		System.out.println("branchFactor="+_branchFactor);
		System.out.println("minimum value count="+_minValueCount);
		System.out.println("nodeSize="+_nodeSize);
		*/
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the file that this BTree operates on.
	 */
	public File getFile() {
		return _file;
	}

	/**
	 * Closes any opened files and release any resources used by this B-Tree.
	 * Once the B-Tree has been closes, it can no longer be used.
	 */
	public void close()
		throws IOException
	{
		synchronized (_nodesInUse) {
			_nodesInUse.clear();
		}
		synchronized (_mruNodes) {
			_mruNodes.clear();
		}

		_raf.close();

		_fileChannel = null;
		_raf = null;
		_file = null;
	}

	/**
	 * Writes any changes that are cached in memory to disk.
	 * 
	 * @throws IOException
	 */
	public void sync()
		throws IOException
	{
		// Write any changed nodes that still reside in the cache to disk
		synchronized (_nodesInUse) {
			for (Node node : _nodesInUse) {
				if (node.dataChanged()) {
					node.write();
				}
			}
		}

		synchronized (_mruNodes) {
			for (Node node : _mruNodes) {
				if (node.dataChanged()) {
					node.write();
				}
			}
		}
	}

	/**
	 * Gets the value that matches the specified key.
	 *
	 * @param key A value that is equal to the value that should be
	 * retrieved, at least as far as the BTreeValueComparator of this
	 * BTree is concerned.
	 * @return The value matching the key, or <tt>null</tt> if no such
	 * value could be found.
	 */
	public byte[] get(byte[] key)
		throws IOException
	{
		if (_rootNodeID == 0) {
			// Empty BTree
			return null;
		}

		Node node = _readNode(_rootNodeID);

		while (true) {
			int valueIdx = node.search(key);

			if (valueIdx >= 0) {
				// Return matching value
				byte[] result = node.getValue(valueIdx);
				node.release();
				return result;
			}
			else if (!node.isLeaf()) {
				// Returned index references the first value that is larger than
				// the key, search the child node just left of it (==same index).
				Node childNode = node.getChildNode(-valueIdx - 1);
				node.release();
				node = childNode;
			}
			else {
				// value not found
				node.release();
				return null;
			}
		}
	}

	/**
	 * Returns an iterator that iterates over all values in this B-Tree.
	 */
	public BTreeIterator iterateAll() {
		return new SeqScanIterator(null, null);
	}

	/**
	 * Returns an iterator that iterates over all values between minValue and
	 * maxValue, inclusive.
	 */
	public BTreeIterator iterateRange(byte[] minValue, byte[] maxValue) {
		return new RangeIterator(null, null, minValue, maxValue);
	}

	/**
	 * Returns an iterator that iterates over all values and returns the values
	 * that match the supplied searchKey after searchMask has been applied to
	 * the value.
	 */
	public BTreeIterator iterateValues(byte[] searchKey, byte[] searchMask) {
		return new SeqScanIterator(searchKey, searchMask);
	}

	/**
	 * Returns an iterator that iterates over all values between minValue and
	 * maxValue (inclusive) and returns the values that match the supplied
	 * searchKey after searchMask has been applied to the value.
	 */
	public BTreeIterator iterateValues(byte[] searchKey, byte[] searchMask, byte[] minValue, byte[] maxValue) {
		return new RangeIterator(searchKey, searchMask, minValue, maxValue);
	}

	/**
	 * Inserts the supplied value into the B-Tree. In case an equal value is
	 * already present in the B-Tree this value is overwritten with the new
	 * value and the old value is returned by this method.
	 *
	 * @param value The value to insert into the B-Tree.
	 * @return The old value that was replaced, if any.
	 * @throws IOException If an I/O error occurred.
	 */
	public byte[] insert(byte[] value)
		throws IOException
	{
		Node rootNode = null;

		if (_rootNodeID == 0) {
			// Empty B-Tree, create a root node
			rootNode = _createNewNode();
			_rootNodeID = rootNode.getID();
			_writeFileHeader();
		}
		else {
			rootNode = _readNode(_rootNodeID);
		}

		InsertResult insertResult = _insertInTree(value, 0, rootNode);

		if (insertResult.overflowValue != null) {
			// Root node overflowed, create a new root node and insert overflow value-nodeID pair in it
			Node newRootNode = _createNewNode();
			newRootNode.setChildNodeID(0, rootNode.getID());
			newRootNode.insertValueNodeIDPair(0, insertResult.overflowValue, insertResult.overflowNodeID);

			_rootNodeID = newRootNode.getID();
			_writeFileHeader();
			newRootNode.release();
		}

		rootNode.release();

		return insertResult.oldValue;
	}

	private InsertResult _insertInTree(byte[] value, int nodeID, Node node)
		throws IOException
	{
		InsertResult insertResult = null;

		// Search value in node
		int valueIdx = node.search(value);

		if (valueIdx >= 0) {
			// Found an equal value, replace it
			insertResult = new InsertResult();
			insertResult.oldValue = node.getValue(valueIdx);

			// Do not replace the value if it's identical to the old
			// value to prevent possibly unnecessary disk writes
			if (!Arrays.equals(value, insertResult.oldValue)) {
				node.setValue(valueIdx, value);
			}
		}
		else {
			// valueIdx references the first value that is larger than the key
			valueIdx = -valueIdx - 1;

			if (node.isLeaf()) {
				// Leaf node, insert value here
				insertResult = _insertInNode(value, nodeID, valueIdx, node);
			}
			else {
				// Not a leaf node, insert value in the child node just left of
				// the found value (==same index)
				Node childNode = node.getChildNode(valueIdx);
				insertResult = _insertInTree(value, nodeID, childNode);
				childNode.release();

				if (insertResult.overflowValue != null) {
					// Child node overflowed, insert overflow in this node
					byte[] oldValue = insertResult.oldValue;
					insertResult = _insertInNode(insertResult.overflowValue, insertResult.overflowNodeID, valueIdx, node);
					insertResult.oldValue = oldValue;
				}
			}
		}

		return insertResult;
	}

	private InsertResult _insertInNode(byte[] value, int nodeID, int valueIdx, Node node)
		throws IOException
	{
		InsertResult insertResult = new InsertResult();

		if (node.isFull()) {
			// Leaf node is full and needs to be split
			Node newNode = _createNewNode();
			insertResult.overflowValue = node.splitAndInsert(value, nodeID, valueIdx, newNode);
			insertResult.overflowNodeID = newNode.getID();
			newNode.release();
		}
		else {
			// Leaf node is not full, simply add the value to it
			node.insertValueNodeIDPair(valueIdx, value, nodeID);
		}

		return insertResult;
	}

	/**
	 * struct-like class used to represent the result of an insert operation.
	 */
	private static class InsertResult {

		/**
		 * The old value that has been replaced by the insertion of a new value.
		 */
		byte[] oldValue = null;

		/**
		 * The value that was removed from a child node due to overflow.
		 */
		byte[] overflowValue = null;

		/**
		 * The nodeID to the right of 'overflowValue' that was removed from a
		 * child node due to overflow.
		 */
		int overflowNodeID = 0;
	}

	/**
	 * Removes the value that matches the specified key from the B-Tree.
	 *
	 * @param key A key that matches the value that should be removed from the B-Tree.
	 * @return The value that was removed from the B-Tree, or <tt>null</tt> if no
	 * matching value was found.
	 * @throws IOException If an I/O error occurred.
	 */
	public byte[] remove(byte[] key)
		throws IOException
	{
		byte[] result = null;

		if (_rootNodeID != 0) {
			Node rootNode = _readNode(_rootNodeID);

			result = _removeFromTree(key, rootNode);

			if (result != null) {
				if (rootNode.isEmpty()) {
					// Collapse B-Tree one level
					Node newRootNode = rootNode.getChildNode(0);
					newRootNode.clearParentInfo();

					_rootNodeID = newRootNode.getID();
					_writeFileHeader();

					newRootNode.release();

					rootNode.setChildNodeID(0, 0);
				}
			}

			rootNode.release();
		}

		return result;
	}

	/**
	 * Removes the value that matches the specified key from the tree starting
	 * at the specified node and returns the removed value.
	 *
	 * @param key A key that matches the value that should be removed from the B-Tree.
	 * @param node The root of the (sub) tree.
	 * @return The value that was removed from the B-Tree, or <tt>null</tt> if no
	 * matching value was found.
	 * @throws IOException If an I/O error occurred.
	 */
	private byte[] _removeFromTree(byte[] key, Node node)
		throws IOException
	{
		byte[] value = null;

		// Search key
		int valueIdx = node.search(key);

		if (valueIdx >= 0) {
			// Found matching value in this node, remove it
			if (node.isLeaf()) {
				value = node.removeValueRight(valueIdx);
			}
			else {
				// Replace the matching value with the smallest value from the right child node
				value = node.getValue(valueIdx);

				Node rightChildNode = node.getChildNode(valueIdx + 1);
				byte[] smallestValue = _removeSmallestValueFromTree(rightChildNode);

				node.setValue(valueIdx, smallestValue);

				_balanceChildNode(node, rightChildNode, valueIdx + 1);

				rightChildNode.release();
			}
		}
		else if (!node.isLeaf()) {
			// Recurse into left child node
			valueIdx = -valueIdx - 1;
			Node childNode = node.getChildNode(valueIdx);
			value = _removeFromTree(key, childNode);

			_balanceChildNode(node, childNode, valueIdx);

			childNode.release();
		}

		return value;
	}

	/**
	 * Removes the smallest value from the tree starting at the specified node
	 * and returns the removed value.
	 *
	 * @param node The root of the (sub) tree.
	 * @return The value that was removed from the B-Tree, or <tt>null</tt> if
	 * the supplied node is empty.
	 * @throws IOException If an I/O error occurred.
	 */
	private byte[] _removeSmallestValueFromTree(Node node)
		throws IOException
	{
		byte[] value = null;

		if (node.isLeaf() && !node.isEmpty()) {
			value = node.removeValueLeft(0);
		}
		else {
			// Recurse into left-most child node
			Node childNode = node.getChildNode(0);
			value = _removeSmallestValueFromTree(childNode);
			_balanceChildNode(node, childNode, 0);
			childNode.release();
		}

		return value;
	}

	private void _balanceChildNode(Node parentNode, Node childNode, int childIdx)
		throws IOException
	{
		if (childNode.getValueCount() < _minValueCount) {
			// Child node contains too few values, try to borrow one from its right sibling
			Node rightSibling = (childIdx < parentNode.getValueCount()) ? parentNode.getChildNode(childIdx+1) : null;

			if (rightSibling != null && rightSibling.getValueCount() > _minValueCount) {
				// Right sibling has enough values to give one up
				childNode.insertValueNodeIDPair(childNode.getValueCount(),
						parentNode.getValue(childIdx), rightSibling.getChildNodeID(0));
				parentNode.setValue(childIdx, rightSibling.removeValueLeft(0));
			}
			else {
				// Right sibling does not have enough values to give one up, try its left sibling
				Node leftSibling = (childIdx > 0) ? parentNode.getChildNode(childIdx - 1) : null;

				if (leftSibling != null && leftSibling.getValueCount() > _minValueCount) {
					// Left sibling has enough values to give one up
					childNode.insertNodeIDValuePair(0,
							leftSibling.getChildNodeID(leftSibling.getValueCount()),
							parentNode.getValue(childIdx-1));
					parentNode.setValue(childIdx-1, leftSibling.removeValueRight(leftSibling.getValueCount()-1));
				}
				else {
					// Both siblings contain the minimum amount of values,
					// merge the child node with its left or right sibling
					if (leftSibling != null) {
						leftSibling.mergeWithRightSibling(parentNode.removeValueRight(childIdx - 1), childNode);
					}
					else {
						childNode.mergeWithRightSibling(parentNode.removeValueRight(childIdx), rightSibling);
					}
				}

				if (leftSibling != null) {
					leftSibling.release();
				}
			}

			if (rightSibling != null) {
				rightSibling.release();
			}
		}
	}

	/**
	 * Removes all values from the B-Tree.
	 *
	 * @throws IOException If an I/O error occurred.
	 */
	public void clear()
		throws IOException
	{
		synchronized (_nodesInUse) {
			_nodesInUse.clear();
		}
		synchronized (_mruNodes) {
			_mruNodes.clear();
		}
		_fileChannel.truncate(HEADER_LENGTH);

		_rootNodeID = 0;
		_maxNodeID = 0;
		_writeFileHeader();
	}

	private Node _createNewNode()
		throws IOException
	{
		Node node = new Node(++_maxNodeID);
		node.use();
		synchronized (_nodesInUse) {
			_nodesInUse.add(node);
		}
		return node;
	}

	private Node _readNode(int id)
		throws IOException
	{
		// Check _nodesInUse list
		synchronized (_nodesInUse) {
			for (Node node : _nodesInUse) {
				if (node.getID() == id) {
					node.use();
					return node;
				}
			}

			// Check _mruNodes list
			synchronized (_mruNodes) {
				Iterator<Node> iter = _mruNodes.iterator();
				while (iter.hasNext()) {
					Node node = iter.next();

					if (node.getID() == id) {
						iter.remove();
						_nodesInUse.add(node);

						node.use();
						return node;
					}
				}
			}

			// Read node from disk
			Node node = new Node(id);
			node.read();
			_nodesInUse.add(node);

			node.use();
			return node;
		}
	}

	private void _releaseNode(Node node)
		throws IOException
	{
		synchronized (_nodesInUse) {
			_nodesInUse.remove(node);

			synchronized (_mruNodes) {
				if (_mruNodes.size() >= MRU_CACHE_SIZE) {
					// Remove least recently used node
					Node lruNode = _mruNodes.removeLast();
					if (lruNode.dataChanged()) {
						lruNode.write();
					}
				}
				_mruNodes.addFirst(node);
			}
		}
	}

	private long _nodeID2offset(int id) {
		return (long)_blockSize * id;
	}

	private int _offset2nodeID(long offset) {
		return (int)(offset / _blockSize);
	}

	private void _writeFileHeader()
		throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(HEADER_LENGTH);
		buf.putInt(FILE_FORMAT_VERSION); // for backwards compatibility in future versions
		buf.putInt(_blockSize);
		buf.putInt(_valueSize);
		buf.putInt(_rootNodeID);

		buf.rewind();

		_fileChannel.write(buf, 0L);
	}

	private void _readFileHeader()
		throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(HEADER_LENGTH);
		_fileChannel.read(buf, 0L);

		buf.rewind();

		@SuppressWarnings("unused") int fileFormatVersion = buf.getInt();
		_blockSize = buf.getInt();
		_valueSize = buf.getInt();
		_rootNodeID = buf.getInt();
	}

	/*------------------*
	 * Inner class Node *
	 *------------------*/

	private class Node {
		
		/** This node's ID. */
		private int _id;

		/** The offset of this node in the file. */
		private long _offset;

		/** This node's data. */
		private byte[] _data;

		/** The number of values containined in this node. */
		private int _valueCount;

		/** The number of objects currently 'using' this node. */
		private int _usageCount;

		/** Flag indicating whether the contents of _data has changed. */
		private boolean _dataChanged;

		/** The node's parent, if any. */
		private Node _parent;

		/** The index of this node in its parent node. */
		private int _indexInParent;

		public Node(int id) {
			_id = id;
			_offset = _nodeID2offset(_id);
			_valueCount = 0;
			_usageCount = 0;

			// Allocate enough room to store one more value and node ID;
			// this greatly simplifies the algorithm for splitting a node.
			_data = new byte[_nodeSize + _slotSize];
		}

		public int getID() {
			return _id;
		}

		public boolean isLeaf() {
			return ByteArrayUtil.getInt(_data, 4) == 0;
		}

		public void use() {
			_usageCount++;
		}

		public void release()
			throws IOException
		{
			_usageCount--;

			if (_usageCount == 0) {
				_releaseNode(this);
			}
		}

		public int getUsageCount() {
			return _usageCount;
		}

		public boolean dataChanged() {
			return _dataChanged;
		}

		public int getValueCount() {
			return _valueCount;
		}

		public boolean isEmpty() {
			return _valueCount == 0;
		}

		public boolean isFull() {
			return _valueCount == _branchFactor - 1;
		}

		public byte[] getValue(int valueIdx) {
			return ByteArrayUtil.get(_data, _valueIdx2offset(valueIdx), _valueSize);
		}

		public void setValue(int valueIdx, byte[] value) {
			ByteArrayUtil.put(value, _data, _valueIdx2offset(valueIdx));
			_dataChanged = true;
		}

		/**
		 * Removes the value that can be found at the specified valueIdx and the
		 * node ID directly to the right of it.
		 *
		 * @param valueIdx A legal value index.
		 * @return The value that was removed.
		 * @see #removeValueLeft
		 */
		public byte[] removeValueRight(int valueIdx) {
			byte[] value = getValue(valueIdx);

			int endOffset = _valueIdx2offset(_valueCount);

			if (valueIdx < _valueCount - 1) {
				// Shift the rest of the data one slot to the left
				_shiftData(_valueIdx2offset(valueIdx+1), endOffset, -_slotSize);
			}

			// Clear last slot
			_clearData(endOffset - _slotSize, endOffset);

			_setValueCount(--_valueCount);

			_dataChanged = true;

			return value;
		}

		/**
		 * Removes the value that can be found at the specified valueIdx and the
		 * node ID directly to the left of it.
		 *
		 * @param valueIdx A legal value index.
		 * @return The value that was removed.
		 * @see #removeValueRight
		 */
		public byte[] removeValueLeft(int valueIdx) {
			byte[] value = getValue(valueIdx);

			int endOffset = _valueIdx2offset(_valueCount);

			// Move the rest of the data one slot to the left
			_shiftData(_nodeIdx2offset(valueIdx+1), endOffset, -_slotSize);

			// Clear last slot
			_clearData(endOffset - _slotSize, endOffset);

			_setValueCount(--_valueCount);

			_dataChanged = true;

			return value;
		}

		public int getChildNodeID(int nodeIdx) {
			return ByteArrayUtil.getInt(_data, _nodeIdx2offset(nodeIdx));
		}

		public void setChildNodeID(int nodeIdx, int nodeID) {
			ByteArrayUtil.putInt(nodeID, _data, _nodeIdx2offset(nodeIdx));
			_dataChanged = true;
		}

		public Node getChildNode(int nodeIdx)
			throws IOException
		{
			int childNodeID = getChildNodeID(nodeIdx);
			Node childNode = _readNode(childNodeID);
			childNode._parent = this;
			childNode._indexInParent = nodeIdx;
			return childNode;
		}

		public Node getParentNode() {
			return _parent;
		}
		
		public int getIndexInParent() {
			return _indexInParent;
		}

		public void clearParentInfo() {
			_parent = null;
			_indexInParent = -1;
		}

		/**
		 * Searches the node for values that match the specified key and returns
		 * its index. If no such value can be found, the index of the first
		 * value that is larger is returned as a negative value by multiplying
		 * the index with -1 and substracting 1 (result = -index - 1). The index
		 * can be calculated from this negative value using the same function,
		 * i.e.: index = -result - 1.
		 */
		public int search(byte[] key) {
			int low = 0;
			int high = _valueCount - 1;

			while (low <= high) {
				int mid = (low + high) >> 1;
				int diff = _comparator.compareBTreeValues(key, _data, _valueIdx2offset(mid), _valueSize);

				if (diff < 0) {
					// key smaller than middle value
					high = mid - 1;
				}
				else if (diff > 0) {
					// key larger than middle value
					low = mid + 1;
				}
				else {
					// key equal to middle value
					return mid;
				}
			}
			return -low - 1;
		}

		public void insertValueNodeIDPair(int valueIdx, byte[] value, int nodeID) {
			int offset = _valueIdx2offset(valueIdx);

			if (valueIdx < _valueCount) {
				// Shift values right of <offset> to the right
				_shiftData(offset, _valueIdx2offset(_valueCount), _slotSize);
			}

			// Insert the new value-nodeID pair
			ByteArrayUtil.put(value, _data, offset);
			ByteArrayUtil.putInt(nodeID, _data, offset + _valueSize);

			// Raise the value count
			_setValueCount(++_valueCount);

			_dataChanged = true;
		}

		public void insertNodeIDValuePair(int nodeIdx, int nodeID, byte[] value) {
			int offset = _nodeIdx2offset(nodeIdx);

			// Shift values right of <offset> to the right
			_shiftData(offset, _valueIdx2offset(_valueCount), _slotSize);

			// Insert the new slot
			ByteArrayUtil.putInt(nodeID, _data, offset);
			ByteArrayUtil.put(value, _data, offset + 4);

			// Raise the value count
			_setValueCount(++_valueCount);

			_dataChanged = true;
		}

		/**
		 * Splits the node, moving half of its values to the supplied new node,
		 * inserting the supplied value-nodeID pair and returning the median
		 * value. The behaviour of this method when called on a node that isn't
		 * full is not specified and can produce unexpected results!
		 */
		public byte[] splitAndInsert(byte[] newValue, int newNodeID, int newValueIdx, Node newNode) {
			// First store the new value-node pair in _data, then split it. This can
			// be done because _data got one spare slot when it was allocated.
			insertValueNodeIDPair(newValueIdx, newValue, newNodeID);

			// Node now contains exactly [_branchFactor] values. The median
			// value at index [_branchFactor/2] is moved to the parent
			// node, the values left of the median stay in this node, the
			// values right of the median are moved to the new node.
			int medianIdx = _branchFactor / 2;
			int medianOffset = _valueIdx2offset(medianIdx);
			int splitOffset = medianOffset + _valueSize;

			// Move all data (including the spare slot) to the right of <splitOffset> to the new node
			System.arraycopy(_data, splitOffset, newNode._data, 4, _data.length - splitOffset);

			// Get the median value
			byte[] medianValue = getValue(medianIdx);

			// Clear the right half of the data in this node
			_clearData(medianOffset, _data.length);

			// Update the value counts
			_setValueCount(medianIdx);
			newNode._setValueCount(_branchFactor - medianIdx - 1);
			newNode._dataChanged = true;

			// Return the median value; it should be inserted into the parent node
			return medianValue;
		}

		public void mergeWithRightSibling(byte[] medianValue, Node rightSibling) {
			// Append median value from parent node
			setValue(_valueCount, medianValue);

			// Append all values and node references from right sibling
			System.arraycopy(
					rightSibling._data, 4,
					_data, _nodeIdx2offset(_valueCount+1),
					_valueIdx2offset(rightSibling._valueCount) - 4);

			_setValueCount(_valueCount + 1 + rightSibling._valueCount);

			rightSibling._clearData(4, _valueIdx2offset(rightSibling._valueCount));
			rightSibling._setValueCount(0);
			rightSibling._dataChanged = true;
		}

		public void read()
			throws IOException
		{
			ByteBuffer buf = ByteBuffer.wrap(_data);
			// Don't fill the spare slot in _data:
			buf.limit(_nodeSize);
			_fileChannel.read(buf, _offset);

			_valueCount = ByteArrayUtil.getInt(_data, 0);
		}

		public void write()
			throws IOException
		{
			ByteBuffer buf = ByteBuffer.wrap(_data);
			// Don't write the spare slot in _data to the file:
			buf.limit(_nodeSize);
			_fileChannel.write(buf, _offset);
			_dataChanged = false;
		}

		/**
		 * Shifts the data between <tt>startOffset</tt> (inclusive) and
		 * <tt>endOffset</tt> (exclusive) <tt>shift</tt> positions to the right.
		 * Negative shift values can be used to shift data to the left.
		 */
		private void _shiftData(int startOffset, int endOffset, int shift) {
			System.arraycopy(_data, startOffset, _data, startOffset + shift, endOffset - startOffset);
		}

		/**
		 * Clears the data between <tt>startOffset</tt> (inclusive) and
		 * <tt>endOffset</tt> (exclusive). All bytes in this range will be set
		 * to 0.
		 */
		private void _clearData(int startOffset, int endOffset) {
			Arrays.fill(_data, startOffset, endOffset, (byte)0);
		}

		private void _setValueCount(int valueCount) {
			_valueCount = valueCount;
			ByteArrayUtil.putInt(_valueCount, _data, 0);
		}

		private int _valueIdx2offset(int id) {
			return 8 + id * _slotSize;
		}
		
		private int _nodeIdx2offset(int id) {
			return 4 + id * _slotSize;
		}
	}

	/*-----------------------------*
	 * Inner class SeqScanIterator *
	 *-----------------------------*/

	private class SeqScanIterator implements BTreeIterator {

		private byte[] _searchKey;
		private byte[] _searchMask;

		private int _currentNodeID;
		private Node _currentNode;
		private int _currentIdx;

		public SeqScanIterator(byte[] searchKey, byte[] searchMask) {
			_searchKey = searchKey;
			_searchMask = searchMask;
		}

		public byte[] next()
			throws IOException
		{
			while (_currentNodeID <= _maxNodeID) {
				if (_currentNode == null) {
					// Read first node
					_currentNodeID = 1;
					_currentNode = _readNode(_currentNodeID);
					_currentIdx = 0;
				}

				while (_currentIdx < _currentNode.getValueCount()) {
					byte[] value = _currentNode.getValue(_currentIdx++);

					if (_searchKey == null || ByteArrayUtil.matchesPattern(value, _searchMask, _searchKey)) {
						// Found a matches value
						return value;
					}
				}

				_currentNode.release();

				_currentNodeID++;
				_currentNode = (_currentNodeID <= _maxNodeID) ? _readNode(_currentNodeID) : null;
				_currentIdx = 0;
			}

			return null;
		}

		public void close()
			throws IOException
		{
			if (_currentNode != null) {
				_currentNodeID = _maxNodeID + 1;

				_currentNode.release();
				_currentNode = null;
			}
		}
	}

	/*---------------------------*
	 * Inner class RangeIterator *
	 *---------------------------*/

	private class RangeIterator implements BTreeIterator {

		private byte[] _searchKey;
		private byte[] _searchMask;

		private byte[] _minValue;
		private byte[] _maxValue;

		private boolean _started;

		private Node _currentNode;
		private int _currentIdx;

		public RangeIterator(byte[] searchKey, byte[] searchMask, byte[] minValue, byte[] maxValue) {
			_searchKey = searchKey;
			_searchMask = searchMask;
			_minValue = minValue;
			_maxValue = maxValue;
			_started = false;
		}

		public byte[] next()
			throws IOException
		{
			if (!_started) {
				_started = true;
				_findMinimum();
			}

			byte[] value = _findNext(false);
			while (value != null) {
				if (_maxValue != null && _comparator.compareBTreeValues(_maxValue, value, 0, value.length) < 0) {
					// Reached maximum value, stop iterating
					while (_currentNode != null) {
						Node parentNode = _currentNode.getParentNode();
						_currentNode.release();
						_currentNode = parentNode;
					}
					value = null;
					break;
				}
				else if (_searchKey != null && !ByteArrayUtil.matchesPattern(value, _searchMask, _searchKey)) {
					// Value doesn't match search key/mask
					value = _findNext(false);
					continue;
				}
				else {
					// Matching value found
					break;
				}
			}

			return value;
		}

		private void _findMinimum()
			throws IOException
		{
			if (_rootNodeID == 0) {
				// Empty BTree
				return;
			}

			_currentNode = _readNode(_rootNodeID);

			// Search first value >= _minValue, or the left-most value in case _minValue is null
			while (true) {
				if (_minValue != null) {
					_currentIdx = _currentNode.search(_minValue);

					if (_currentIdx >= 0) {
						// Found exact match with minimum value
						break;
					}
					else {
						// _currentIdx indicates the first value larger than the minimum value
						_currentIdx = -_currentIdx - 1;
					}
				}

				if (_currentNode.isLeaf()) {
					break;
				}
				else {
					_currentNode = _currentNode.getChildNode(_currentIdx);
					_currentIdx = 0;
				}
			}
		}

		private byte[] _findNext(boolean returnedFromRecursion)
			throws IOException
		{
			if (_currentNode == null) {
				return null;
			}

			if (returnedFromRecursion || _currentNode.isLeaf()) {
				if (_currentIdx >= _currentNode.getValueCount()) {
					// No more values in this node, continue with parent node
					Node parentNode = _currentNode.getParentNode();
					_currentIdx = _currentNode.getIndexInParent();
					_currentNode.release();
					_currentNode = parentNode;
					return _findNext(true);
				}
				else {
					return _currentNode.getValue(_currentIdx++);
				}
			}
			else {
				_currentNode = _currentNode.getChildNode(_currentIdx);
				_currentIdx = 0;
				return _findNext(false);
			}
		}

		public void close()
			throws IOException
		{
			if (_currentNode != null) {
				_currentNode.release();
				_currentNode = null;
			}
		}
	}

	/*--------------*
	 * Test methods *
	 *--------------*/

	public static void main(String[] args)
		throws Exception
	{
		System.out.println("Running BTree test...");
		if (args.length > 1) {
			runPerformanceTest(args);
		}
		else {
			runDebugTest(args);
		}
		System.out.println("Done.");
	}

	public static void runPerformanceTest(String[] args)
		throws Exception
	{
		File dataFile = new File(args[0]);
		int valueCount = Integer.parseInt(args[1]);
		BTreeValueComparator comparator = new DefaultBTreeValueComparator();
		BTree btree = new BTree(dataFile, 501, 13, comparator);

		java.util.Random random = new java.util.Random(0L);
		byte[] value = new byte[13];

		long startTime = System.currentTimeMillis();
		for (int i = 1; i <= valueCount; i++) {
			random.nextBytes(value);
			btree.insert(value);
			if (i % 50000 == 0) {
				System.out.println("Inserted " + i + " values in " + (System.currentTimeMillis()-startTime) + " ms");
			}
		}

		System.out.println("Iterating over all values in sequential order...");
		startTime = System.currentTimeMillis();
		BTreeIterator iter = btree.iterateAll();
		value = iter.next();
		int count = 0;
		while (value != null) {
			count++;
			value = iter.next();
		}
		System.out.println("Iteration over " + count + " items finished in " + (System.currentTimeMillis()-startTime) + " ms");
	}

	public static void runDebugTest(String[] args)
		throws Exception
	{
		File dataFile = new File(args[0]);
		BTree btree = new BTree(dataFile, 28, 1);

		btree.print(System.out);

/*
		System.out.println("Adding values...");
		btree.startTransaction();
		btree.insert("C".getBytes());
		btree.insert("N".getBytes());
		btree.insert("G".getBytes());
		btree.insert("A".getBytes());
		btree.insert("H".getBytes());
		btree.insert("E".getBytes());
		btree.insert("K".getBytes());
		btree.insert("Q".getBytes());
		btree.insert("M".getBytes());
		btree.insert("F".getBytes());
		btree.insert("W".getBytes());
		btree.insert("L".getBytes());
		btree.insert("T".getBytes());
		btree.insert("Z".getBytes());
		btree.insert("D".getBytes());
		btree.insert("P".getBytes());
		btree.insert("R".getBytes());
		btree.insert("X".getBytes());
		btree.insert("Y".getBytes());
		btree.insert("S".getBytes());
		btree.commitTransaction();
		btree.print(System.out);

		System.out.println("Removing values...");

		System.out.println("Removing H...");
		btree.remove("H".getBytes());
		btree.commitTransaction();
		btree.print(System.out);

		System.out.println("Removing T...");
		btree.remove("T".getBytes());
		btree.commitTransaction();
		btree.print(System.out);

		System.out.println("Removing R...");
		btree.remove("R".getBytes());
		btree.commitTransaction();
		btree.print(System.out);

		System.out.println("Removing E...");
		btree.remove("E".getBytes());
		btree.commitTransaction();
		btree.print(System.out);

		System.out.println("Values from I to U:");
		BTreeIterator iter = btree.iterateRange("I".getBytes(), "V".getBytes());
		byte[] value = iter.next();
		while (value != null) {
			System.out.print(new String(value) + " ");
			value = iter.next();
		}
		System.out.println();
		*/
	}

	public void print(PrintStream out)
		throws IOException
	{
		out.println("---contents of BTree file---");
		out.println("Stored parameters:");
		out.println("block size   = " + _blockSize);
		out.println("value size   = " + _valueSize);
		out.println("root node ID = " + _rootNodeID);
		out.println();
		out.println("Derived parameters:");
		out.println("slot size       = " + _slotSize);
		out.println("branch factor   = " + _branchFactor);
		out.println("min value count = " + _minValueCount);
		out.println("node size       = " + _nodeSize);
		out.println("max node ID     = " + _maxNodeID);
		out.println();

		ByteBuffer buf = ByteBuffer.allocate(_nodeSize);
		for (long offset = _blockSize; offset < _fileChannel.size(); offset += _blockSize) {
			_fileChannel.read(buf, offset);
			buf.rewind();

			int nodeID = _offset2nodeID(offset);
			int count = buf.getInt();
			out.print("node " + nodeID + ": ");
			out.print("count="+count+ " ");

			byte[] value = new byte[_valueSize];

			for (int i = 0; i < count; i++) {
				// node ID
				out.print(buf.getInt());

				// value
				buf.get(value);
				out.print("["+ByteArrayUtil.toHexString(value)+"]");
				//out.print("["+new String(value)+"]");
			}

			// last node ID
			out.println(buf.getInt());

			buf.clear();
		}
		out.println("---end of BTree file---");
	}
}
