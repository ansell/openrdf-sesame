/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
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
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;

import info.aduna.io.ByteArrayUtil;

/**
 * Implementation of an on-disk B-Tree using the <tt>java.nio</tt> classes
 * that are available in JDK 1.4 and newer. Documentation about B-Trees can be
 * found on-line at the following URLs:
 * <ul>
 * <li>http://cis.stvincent.edu/swd/btree/btree.html</li>,
 * <li>http://bluerwhite.org/btree/</li>, and
 * <li>http://semaphorecorp.com/btp/algo.html</li>.
 * </ul>
 * The first reference was used to implement this class.
 * <p>
 * TODO: clean up code
 * 
 * @author Arjohn Kampman
 */
public class BTree {

	/*-----------------*
	 * Class constants *
	 *-----------------*/

	private static final int FILE_FORMAT_VERSION = 1;

	private static final int HEADER_LENGTH = 16;

	private static final int MRU_CACHE_SIZE = 8;

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * The BTree file.
	 */
	private final File file;

	/**
	 * A RandomAccessFile used to open and close a read/write FileChannel on the
	 * BTree file.
	 */
	private final RandomAccessFile raf;

	/**
	 * The file channel used to read and write data from/to the BTree file.
	 */
	private final FileChannel fileChannel;

	/**
	 * Flag indicating whether file writes should be forced to disk using
	 * {@link FileChannel#force(boolean)}.
	 */
	private final boolean forceSync;

	/**
	 * Object used to determine whether one value is lower, equal or greater than
	 * another value. This determines the order of values in the BTree.
	 */
	private final RecordComparator comparator;

	/**
	 * List containing nodes that are currently "in use", used for caching.
	 */
	private final LinkedList<Node> nodesInUse = new LinkedList<Node>();

	/**
	 * List containing the most recently released nodes, used for caching.
	 */
	private final LinkedList<Node> mruNodes = new LinkedList<Node>();;

	/**
	 * Bit set recording which nodes have been allocated, using node IDs as
	 * index.
	 */
	private final BitSet allocatedNodes = new BitSet();

	/**
	 * Flag indicating whether {@link #allocatedNodes} has been initialized.
	 */
	private boolean allocatedNodesInitialized = false;

	// Stored or specified properties //

	/**
	 * The block size to use for calculating BTree node size. For optimal
	 * performance, the specified block size should be equal to the file system's
	 * block size.
	 */
	private final int blockSize;

	/**
	 * The size of the values (byte arrays) in this BTree.
	 */
	private final int valueSize;

	// Derived properties //

	/**
	 * The size of a slot storing a node ID and a value.
	 */
	private final int slotSize;

	/**
	 * The maximum number of outgoing branches for a node.
	 */
	private final int branchFactor;

	/**
	 * The minimum number of values for a node (except for the root).
	 */
	private final int minValueCount;

	/**
	 * The size of a node in bytes.
	 */
	private final int nodeSize;

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The ID of the root node, <tt>0</tt> to indicate that there is no root
	 * node (i.e. the BTree is empty).
	 */
	private int rootNodeID;

	/**
	 * The highest ID number of the nodes in this BTree.
	 */
	private int maxNodeID;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new BTree that uses an instance of
	 * <tt>DefaultRecordComparator</tt> to compare values.
	 * 
	 * @param dataFile
	 *        The file for the B-Tree.
	 * @param blockSize
	 *        The size (in bytes) of a file block for a single node. Ideally, the
	 *        size specified is the size of a block in the used file system.
	 * @param valueSize
	 *        The size (in bytes) of the fixed-length values that are or will be
	 *        stored in the B-Tree.
	 * @throws IOException
	 *         In case the initialization of the B-Tree file failed.
	 * @see DefaultRecordComparator
	 */
	public BTree(File dataFile, int blockSize, int valueSize)
		throws IOException
	{
		this(dataFile, blockSize, valueSize, false);
	}

	/**
	 * Creates a new BTree that uses an instance of
	 * <tt>DefaultRecordComparator</tt> to compare values.
	 * 
	 * @param dataFile
	 *        The file for the B-Tree.
	 * @param blockSize
	 *        The size (in bytes) of a file block for a single node. Ideally, the
	 *        size specified is the size of a block in the used file system.
	 * @param valueSize
	 *        The size (in bytes) of the fixed-length values that are or will be
	 *        stored in the B-Tree.
	 * @param forceSync
	 *        Flag indicating whether updates should be synced to disk forcefully
	 *        by calling {@link FileChannel#force(boolean)}. This may have a
	 *        severe impact on write performance.
	 * @throws IOException
	 *         In case the initialization of the B-Tree file failed.
	 * @see DefaultRecordComparator
	 */
	public BTree(File dataFile, int blockSize, int valueSize, boolean forceSync)
		throws IOException
	{
		this(dataFile, blockSize, valueSize, new DefaultRecordComparator(), forceSync);
	}

	/**
	 * Creates a new BTree that uses the supplied <tt>RecordComparator</tt> to
	 * compare the values that are or will be stored in the B-Tree.
	 * 
	 * @param dataFile
	 *        The file for the B-Tree.
	 * @param blockSize
	 *        The size (in bytes) of a file block for a single node. Ideally, the
	 *        size specified is the size of a block in the used file system.
	 * @param valueSize
	 *        The size (in bytes) of the fixed-length values that are or will be
	 *        stored in the B-Tree.
	 * @param comparator
	 *        The <tt>RecordComparator</tt> to use for determining whether one
	 *        value is smaller, larger or equal to another.
	 * @throws IOException
	 *         In case the initialization of the B-Tree file failed.
	 */
	public BTree(File dataFile, int blockSize, int valueSize, RecordComparator comparator)
		throws IOException
	{
		this(dataFile, blockSize, valueSize, comparator, false);
	}

	/**
	 * Creates a new BTree that uses the supplied <tt>RecordComparator</tt> to
	 * compare the values that are or will be stored in the B-Tree.
	 * 
	 * @param dataFile
	 *        The file for the B-Tree.
	 * @param blockSize
	 *        The size (in bytes) of a file block for a single node. Ideally, the
	 *        size specified is the size of a block in the used file system.
	 * @param valueSize
	 *        The size (in bytes) of the fixed-length values that are or will be
	 *        stored in the B-Tree.
	 * @param comparator
	 *        The <tt>RecordComparator</tt> to use for determining whether one
	 *        value is smaller, larger or equal to another.
	 * @param forceSync
	 *        Flag indicating whether updates should be synced to disk forcefully
	 *        by calling {@link FileChannel#force(boolean)}. This may have a
	 *        severe impact on write performance.
	 * @throws IOException
	 *         In case the initialization of the B-Tree file failed.
	 */
	public BTree(File dataFile, int blockSize, int valueSize, RecordComparator comparator, boolean forceSync)
		throws IOException
	{
		if (dataFile == null) {
			throw new IllegalArgumentException("dataFile must not be null");
		}
		if (blockSize < HEADER_LENGTH) {
			throw new IllegalArgumentException("block size must be at least " + HEADER_LENGTH + " bytes");
		}
		if (valueSize <= 0) {
			throw new IllegalArgumentException("value size must be larger than 0");
		}
		if (blockSize < 3 * valueSize + 20) {
			throw new IllegalArgumentException(
					"block size to small; must at least be able to store three values");
		}
		if (comparator == null) {
			throw new IllegalArgumentException("comparator muts not be null");
		}

		this.file = dataFile;
		this.comparator = comparator;
		this.forceSync = forceSync;

		if (!file.exists()) {
			boolean created = file.createNewFile();
			if (!created) {
				throw new IOException("Failed to create file: " + file);
			}
		}

		// Open a read/write channel to the file
		raf = new RandomAccessFile(file, "rw");
		fileChannel = raf.getChannel();

		if (fileChannel.size() == 0L) {
			// Empty file, initialize it with the specified parameters
			this.blockSize = blockSize;
			this.valueSize = valueSize;
			this.rootNodeID = 0;
			this.maxNodeID = 0;

			writeFileHeader();

			sync();
		}
		else {
			// Read parameters from file
			ByteBuffer buf = ByteBuffer.allocate(HEADER_LENGTH);
			fileChannel.read(buf, 0L);

			buf.rewind();

			@SuppressWarnings("unused")
			int fileFormatVersion = buf.getInt();
			this.blockSize = buf.getInt();
			this.valueSize = buf.getInt();
			this.rootNodeID = buf.getInt();

			// Verify that the value sizes match
			if (this.valueSize != valueSize) {
				throw new IOException("Specified value size (" + valueSize
						+ ") is different from what is stored on disk (" + this.valueSize + ")");
			}
		}

		// Calculate derived properties
		slotSize = 4 + this.valueSize;
		branchFactor = 1 + (this.blockSize - 8) / slotSize;
		// bf=30 --> mvc=14; bf=29 --> mvc=14
		minValueCount = (branchFactor - 1) / 2;
		nodeSize = 8 + (branchFactor - 1) * slotSize;
		maxNodeID = Math.max(0, offset2nodeID(fileChannel.size() - nodeSize));

		// System.out.println("blockSize=" + this.blockSize);
		// System.out.println("valueSize=" + this.valueSize);
		// System.out.println("slotSize=" + this.slotSize);
		// System.out.println("branchFactor=" + this.branchFactor);
		// System.out.println("minimum value count=" + this.minValueCount);
		// System.out.println("nodeSize=" + this.nodeSize);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the file that this BTree operates on.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Closes any opened files and release any resources used by this B-Tree. Any
	 * pending changes will be synchronized to disk before closing. Once the
	 * B-Tree has been closes, it can no longer be used.
	 */
	public void close()
		throws IOException
	{
		sync();

		synchronized (nodesInUse) {
			nodesInUse.clear();
		}
		synchronized (mruNodes) {
			mruNodes.clear();
		}

		raf.close();
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
		synchronized (nodesInUse) {
			for (Node node : nodesInUse) {
				if (node.dataChanged()) {
					node.write();
				}
			}
		}

		synchronized (mruNodes) {
			for (Node node : mruNodes) {
				if (node.dataChanged()) {
					node.write();
				}
			}
		}

		if (forceSync) {
			fileChannel.force(false);
		}
	}

	/**
	 * Gets the value that matches the specified key.
	 * 
	 * @param key
	 *        A value that is equal to the value that should be retrieved, at
	 *        least as far as the RecordComparator of this BTree is concerned.
	 * @return The value matching the key, or <tt>null</tt> if no such value
	 *         could be found.
	 */
	public byte[] get(byte[] key)
		throws IOException
	{
		if (rootNodeID == 0) {
			// Empty BTree
			return null;
		}

		Node node = readNode(rootNodeID);

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
	public RecordIterator iterateAll() {
		return new RangeIterator(null, null, null, null);
	}

	/**
	 * Returns an iterator that iterates over all values between minValue and
	 * maxValue, inclusive.
	 */
	public RecordIterator iterateRange(byte[] minValue, byte[] maxValue) {
		return new RangeIterator(null, null, minValue, maxValue);
	}

	/**
	 * Returns an iterator that iterates over all values and returns the values
	 * that match the supplied searchKey after searchMask has been applied to the
	 * value.
	 */
	public RecordIterator iterateValues(byte[] searchKey, byte[] searchMask) {
		return new RangeIterator(searchKey, searchMask, null, null);
	}

	/**
	 * Returns an iterator that iterates over all values between minValue and
	 * maxValue (inclusive) and returns the values that match the supplied
	 * searchKey after searchMask has been applied to the value.
	 */
	public RecordIterator iterateRangedValues(byte[] searchKey, byte[] searchMask, byte[] minValue,
			byte[] maxValue)
	{
		return new RangeIterator(searchKey, searchMask, minValue, maxValue);
	}

	/**
	 * Returns an estimate for the number of values stored in this BTree.
	 */
	public long getValueCountEstimate()
		throws IOException
	{
		int allocatedNodesCount;

		synchronized (allocatedNodes) {
			initAllocatedNodes();
			allocatedNodesCount = allocatedNodes.cardinality();
		}

		// Assume fill factor of 70%
		return (long)(allocatedNodesCount * (branchFactor - 1) * 0.7);
	}

	/**
	 * Inserts the supplied value into the B-Tree. In case an equal value is
	 * already present in the B-Tree this value is overwritten with the new value
	 * and the old value is returned by this method.
	 * 
	 * @param value
	 *        The value to insert into the B-Tree.
	 * @return The old value that was replaced, if any.
	 * @throws IOException
	 *         If an I/O error occurred.
	 */
	public byte[] insert(byte[] value)
		throws IOException
	{
		Node rootNode = null;

		if (rootNodeID == 0) {
			// Empty B-Tree, create a root node
			rootNode = createNewNode();
			rootNodeID = rootNode.getID();
			writeFileHeader();
		}
		else {
			rootNode = readNode(rootNodeID);
		}

		InsertResult insertResult = insertInTree(value, 0, rootNode);

		if (insertResult.overflowValue != null) {
			// Root node overflowed, create a new root node and insert overflow
			// value-nodeID pair in it
			Node newRootNode = createNewNode();
			newRootNode.setChildNodeID(0, rootNode.getID());
			newRootNode.insertValueNodeIDPair(0, insertResult.overflowValue, insertResult.overflowNodeID);

			rootNodeID = newRootNode.getID();
			writeFileHeader();
			newRootNode.release();
		}

		rootNode.release();

		return insertResult.oldValue;
	}

	private InsertResult insertInTree(byte[] value, int nodeID, Node node)
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
				insertResult = insertInNode(value, nodeID, valueIdx, node);
			}
			else {
				// Not a leaf node, insert value in the child node just left of
				// the found value (==same index)
				Node childNode = node.getChildNode(valueIdx);
				insertResult = insertInTree(value, nodeID, childNode);
				childNode.release();

				if (insertResult.overflowValue != null) {
					// Child node overflowed, insert overflow in this node
					byte[] oldValue = insertResult.oldValue;
					insertResult = insertInNode(insertResult.overflowValue, insertResult.overflowNodeID, valueIdx,
							node);
					insertResult.oldValue = oldValue;
				}
			}
		}

		return insertResult;
	}

	private InsertResult insertInNode(byte[] value, int nodeID, int valueIdx, Node node)
		throws IOException
	{
		InsertResult insertResult = new InsertResult();

		if (node.isFull()) {
			// Leaf node is full and needs to be split
			Node newNode = createNewNode();
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
	 * @param key
	 *        A key that matches the value that should be removed from the
	 *        B-Tree.
	 * @return The value that was removed from the B-Tree, or <tt>null</tt> if
	 *         no matching value was found.
	 * @throws IOException
	 *         If an I/O error occurred.
	 */
	public byte[] remove(byte[] key)
		throws IOException
	{
		byte[] result = null;

		if (rootNodeID != 0) {
			Node rootNode = readNode(rootNodeID);

			result = removeFromTree(key, rootNode);

			if (rootNode.isEmpty()) {
				// Root node has become empty as a result of the removal
				if (rootNode.isLeaf()) {
					// Nothing's left
					rootNodeID = 0;
				}
				else {
					// Collapse B-Tree one level
					rootNodeID = rootNode.getChildNodeID(0);
					rootNode.setChildNodeID(0, 0);
				}

				// Write new root node ID to file header
				writeFileHeader();
			}

			rootNode.release();
		}

		return result;
	}

	/**
	 * Removes the value that matches the specified key from the tree starting at
	 * the specified node and returns the removed value.
	 * 
	 * @param key
	 *        A key that matches the value that should be removed from the
	 *        B-Tree.
	 * @param node
	 *        The root of the (sub) tree.
	 * @return The value that was removed from the B-Tree, or <tt>null</tt> if
	 *         no matching value was found.
	 * @throws IOException
	 *         If an I/O error occurred.
	 */
	private byte[] removeFromTree(byte[] key, Node node)
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
				// Replace the matching value with the smallest value from the right
				// child node
				value = node.getValue(valueIdx);

				Node rightChildNode = node.getChildNode(valueIdx + 1);
				byte[] smallestValue = removeSmallestValueFromTree(rightChildNode);

				node.setValue(valueIdx, smallestValue);

				balanceChildNode(node, rightChildNode, valueIdx + 1);

				rightChildNode.release();
			}
		}
		else if (!node.isLeaf()) {
			// Recurse into left child node
			valueIdx = -valueIdx - 1;
			Node childNode = node.getChildNode(valueIdx);
			value = removeFromTree(key, childNode);

			balanceChildNode(node, childNode, valueIdx);

			childNode.release();
		}

		return value;
	}

	/**
	 * Removes the smallest value from the tree starting at the specified node
	 * and returns the removed value.
	 * 
	 * @param node
	 *        The root of the (sub) tree.
	 * @return The value that was removed from the B-Tree, or <tt>null</tt> if
	 *         the supplied node is empty.
	 * @throws IOException
	 *         If an I/O error occurred.
	 */
	private byte[] removeSmallestValueFromTree(Node node)
		throws IOException
	{
		byte[] value = null;

		if (node.isLeaf()) {
			if (!node.isEmpty()) {
				value = node.removeValueLeft(0);
			}
		}
		else {
			// Recurse into left-most child node
			Node childNode = node.getChildNode(0);
			value = removeSmallestValueFromTree(childNode);
			balanceChildNode(node, childNode, 0);
			childNode.release();
		}

		return value;
	}

	private void balanceChildNode(Node parentNode, Node childNode, int childIdx)
		throws IOException
	{
		if (childNode.getValueCount() < minValueCount) {
			// Child node contains too few values, try to borrow one from its right
			// sibling
			Node rightSibling = (childIdx < parentNode.getValueCount()) ? parentNode.getChildNode(childIdx + 1)
					: null;

			if (rightSibling != null && rightSibling.getValueCount() > minValueCount) {
				// Right sibling has enough values to give one up
				childNode.insertValueNodeIDPair(childNode.getValueCount(), parentNode.getValue(childIdx),
						rightSibling.getChildNodeID(0));
				parentNode.setValue(childIdx, rightSibling.removeValueLeft(0));
			}
			else {
				// Right sibling does not have enough values to give one up, try its
				// left sibling
				Node leftSibling = (childIdx > 0) ? parentNode.getChildNode(childIdx - 1) : null;

				if (leftSibling != null && leftSibling.getValueCount() > minValueCount) {
					// Left sibling has enough values to give one up
					childNode.insertNodeIDValuePair(0, leftSibling.getChildNodeID(leftSibling.getValueCount()),
							parentNode.getValue(childIdx - 1));
					parentNode.setValue(childIdx - 1,
							leftSibling.removeValueRight(leftSibling.getValueCount() - 1));
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
	 * @throws IOException
	 *         If an I/O error occurred.
	 */
	public void clear()
		throws IOException
	{
		synchronized (nodesInUse) {
			nodesInUse.clear();
		}
		synchronized (mruNodes) {
			mruNodes.clear();
		}
		fileChannel.truncate(HEADER_LENGTH);

		rootNodeID = 0;
		maxNodeID = 0;
		allocatedNodes.clear();

		writeFileHeader();
	}

	private Node createNewNode()
		throws IOException
	{
		int newNodeID;

		synchronized (allocatedNodes) {
			initAllocatedNodes();
			newNodeID = allocatedNodes.nextClearBit(1);
			allocatedNodes.set(newNodeID);
		}

		if (newNodeID > maxNodeID) {
			maxNodeID = newNodeID;
		}

		Node node = new Node(newNodeID);
		node.use();
		synchronized (nodesInUse) {
			nodesInUse.add(node);
		}
		return node;
	}

	private Node readNode(int id)
		throws IOException
	{
		if (id <= 0) {
			throw new IllegalArgumentException("id must be larger than 0, is: " + id);
		}

		// Check nodesInUse list
		synchronized (nodesInUse) {
			for (Node node : nodesInUse) {
				if (node.getID() == id) {
					node.use();
					return node;
				}
			}

			// Check mruNodes list
			synchronized (mruNodes) {
				Iterator<Node> iter = mruNodes.iterator();
				while (iter.hasNext()) {
					Node node = iter.next();

					if (node.getID() == id) {
						iter.remove();
						nodesInUse.add(node);

						node.use();
						return node;
					}
				}
			}

			// Read node from disk
			Node node = new Node(id);
			node.read();
			nodesInUse.add(node);

			node.use();
			return node;
		}
	}

	private void releaseNode(Node node)
		throws IOException
	{
		synchronized (nodesInUse) {
			nodesInUse.remove(node);

			if (node.isEmpty() && node.isLeaf()) {
				// Discard node
				synchronized (allocatedNodes) {
					initAllocatedNodes();
					allocatedNodes.clear(node.id);
				}
				synchronized (mruNodes) {
					mruNodes.remove(node);
				}

				node.write();

				if (node.id == maxNodeID) {
					// Shrink file
					synchronized (allocatedNodes) {
						maxNodeID = Math.max(0, allocatedNodes.length() - 1);
					}
					fileChannel.truncate(nodeID2offset(maxNodeID) + nodeSize);
				}
			}
			else {
				synchronized (mruNodes) {
					if (mruNodes.size() >= MRU_CACHE_SIZE) {
						// Remove least recently used node
						Node lruNode = mruNodes.removeLast();
						if (lruNode.dataChanged()) {
							lruNode.write();
						}
					}
					mruNodes.addFirst(node);
				}
			}
		}
	}

	private void initAllocatedNodes()
		throws IOException
	{
		if (!allocatedNodesInitialized) {
			if (rootNodeID != 0) {
				initAllocatedNodes(rootNodeID);
			}
			allocatedNodesInitialized = true;
		}
	}

	private void initAllocatedNodes(int nodeID)
		throws IOException
	{
		allocatedNodes.set(nodeID);

		Node node = readNode(nodeID);

		if (!node.isLeaf()) {
			for (int i = 0; i < node.getValueCount() + 1; i++) {
				initAllocatedNodes(node.getChildNodeID(i));
			}
		}

		node.release();
	}

	private long nodeID2offset(int id) {
		return (long)blockSize * id;
	}

	private int offset2nodeID(long offset) {
		return (int)(offset / blockSize);
	}

	private void writeFileHeader()
		throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(HEADER_LENGTH);
		// for backwards compatibility in future versions
		buf.putInt(FILE_FORMAT_VERSION);
		buf.putInt(blockSize);
		buf.putInt(valueSize);
		buf.putInt(rootNodeID);

		buf.rewind();

		fileChannel.write(buf, 0L);
	}

	/*------------------*
	 * Inner class Node *
	 *------------------*/

	private class Node {

		/** This node's ID. */
		private int id;

		/** The offset of this node in the file. */
		private long offset;

		/** This node's data. */
		private byte[] data;

		/** The number of values containined in this node. */
		private int valueCount;

		/** The number of objects currently 'using' this node. */
		private int usageCount;

		/** Flag indicating whether the contents of data has changed. */
		private boolean dataChanged;

		/** Registered listeners that want to be notified of changes to the node. */
		private LinkedList<NodeListener> listeners = new LinkedList<NodeListener>();

		/**
		 * Creates a new Node object with the specified ID.
		 * 
		 * @param id
		 *        The node's ID, must be larger than <tt>0</tt>.
		 * @throws IllegalArgumentException
		 *         If the specified <tt>id</tt> is &lt;= <tt>0</tt>.
		 */
		public Node(int id) {
			if (id <= 0) {
				throw new IllegalArgumentException("id must be larger than 0, is: " + id);
			}

			this.id = id;
			this.offset = nodeID2offset(id);
			this.valueCount = 0;
			this.usageCount = 0;

			// Allocate enough room to store one more value and node ID;
			// this greatly simplifies the algorithm for splitting a node.
			this.data = new byte[nodeSize + slotSize];
		}

		public int getID() {
			return id;
		}

		public boolean isLeaf() {
			return getChildNodeID(0) == 0;
		}

		public void use() {
			usageCount++;
		}

		public void release()
			throws IOException
		{
			assert usageCount > 0 : "Releasing node while usage count is " + usageCount;

			usageCount--;

			if (usageCount == 0) {
				releaseNode(this);
			}
		}

		public int getUsageCount() {
			return usageCount;
		}

		public boolean dataChanged() {
			return dataChanged;
		}

		public int getValueCount() {
			return valueCount;
		}

		public int getNodeCount() {
			if (isLeaf()) {
				return 0;
			}
			else {
				return valueCount + 1;
			}
		}

		/**
		 * Checks if this node has any values.
		 * 
		 * @return <tt>true</tt> if this node has no values, <tt>fals</tt> if
		 *         it has.
		 */
		public boolean isEmpty() {
			return valueCount == 0;
		}

		public boolean isFull() {
			return valueCount == branchFactor - 1;
		}

		public byte[] getValue(int valueIdx) {
			return ByteArrayUtil.get(data, valueIdx2offset(valueIdx), valueSize);
		}

		public void setValue(int valueIdx, byte[] value) {
			ByteArrayUtil.put(value, data, valueIdx2offset(valueIdx));
			dataChanged = true;
			notifyValueChanged(valueIdx);
		}

		/**
		 * Removes the value that can be found at the specified valueIdx and the
		 * node ID directly to the right of it.
		 * 
		 * @param valueIdx
		 *        A legal value index.
		 * @return The value that was removed.
		 * @see #removeValueLeft
		 */
		public byte[] removeValueRight(int valueIdx) {
			byte[] value = getValue(valueIdx);

			int endOffset = valueIdx2offset(valueCount);

			if (valueIdx < valueCount - 1) {
				// Shift the rest of the data one slot to the left
				shiftData(valueIdx2offset(valueIdx + 1), endOffset, -slotSize);
			}

			// Clear last slot
			clearData(endOffset - slotSize, endOffset);

			setValueCount(--valueCount);

			dataChanged = true;

			notifyValueRemoved(valueIdx);

			return value;
		}

		/**
		 * Removes the value that can be found at the specified valueIdx and the
		 * node ID directly to the left of it.
		 * 
		 * @param valueIdx
		 *        A legal value index.
		 * @return The value that was removed.
		 * @see #removeValueRight
		 */
		public byte[] removeValueLeft(int valueIdx) {
			byte[] value = getValue(valueIdx);

			int endOffset = valueIdx2offset(valueCount);

			// Move the rest of the data one slot to the left
			shiftData(nodeIdx2offset(valueIdx + 1), endOffset, -slotSize);

			// Clear last slot
			clearData(endOffset - slotSize, endOffset);

			setValueCount(--valueCount);

			dataChanged = true;

			notifyValueRemoved(valueIdx);

			return value;
		}

		public int getChildNodeID(int nodeIdx) {
			return ByteArrayUtil.getInt(data, nodeIdx2offset(nodeIdx));
		}

		public void setChildNodeID(int nodeIdx, int nodeID) {
			ByteArrayUtil.putInt(nodeID, data, nodeIdx2offset(nodeIdx));
			dataChanged = true;
		}

		public Node getChildNode(int nodeIdx)
			throws IOException
		{
			int childNodeID = getChildNodeID(nodeIdx);
			return readNode(childNodeID);
		}

		/**
		 * Searches the node for values that match the specified key and returns
		 * its index. If no such value can be found, the index of the first value
		 * that is larger is returned as a negative value by multiplying the index
		 * with -1 and substracting 1 (result = -index - 1). The index can be
		 * calculated from this negative value using the same function, i.e.:
		 * index = -result - 1.
		 */
		public int search(byte[] key) {
			int low = 0;
			int high = valueCount - 1;

			while (low <= high) {
				int mid = (low + high) >> 1;
				int diff = comparator.compareBTreeValues(key, data, valueIdx2offset(mid), valueSize);

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
			int offset = valueIdx2offset(valueIdx);

			if (valueIdx < valueCount) {
				// Shift values right of <offset> to the right
				shiftData(offset, valueIdx2offset(valueCount), slotSize);
			}

			// Insert the new value-nodeID pair
			ByteArrayUtil.put(value, data, offset);
			ByteArrayUtil.putInt(nodeID, data, offset + valueSize);

			// Raise the value count
			setValueCount(++valueCount);

			notifyValueAdded(valueIdx);

			dataChanged = true;
		}

		public void insertNodeIDValuePair(int nodeIdx, int nodeID, byte[] value) {
			int offset = nodeIdx2offset(nodeIdx);

			// Shift values right of <offset> to the right
			shiftData(offset, valueIdx2offset(valueCount), slotSize);

			// Insert the new slot
			ByteArrayUtil.putInt(nodeID, data, offset);
			ByteArrayUtil.put(value, data, offset + 4);

			// Raise the value count
			setValueCount(++valueCount);

			notifyValueAdded(nodeIdx);

			dataChanged = true;
		}

		/**
		 * Splits the node, moving half of its values to the supplied new node,
		 * inserting the supplied value-nodeID pair and returning the median
		 * value. The behaviour of this method when called on a node that isn't
		 * full is not specified and can produce unexpected results!
		 * 
		 * @throws IOException
		 */
		public byte[] splitAndInsert(byte[] newValue, int newNodeID, int newValueIdx, Node newNode)
			throws IOException
		{
			// First store the new value-node pair in data, then split it. This
			// can be done because data got one spare slot when it was allocated.
			insertValueNodeIDPair(newValueIdx, newValue, newNodeID);

			// Node now contains exactly [_branchFactor] values. The median
			// value at index [_branchFactor/2] is moved to the parent
			// node, the values left of the median stay in this node, the
			// values right of the median are moved to the new node.
			int medianIdx = branchFactor / 2;
			int medianOffset = valueIdx2offset(medianIdx);
			int splitOffset = medianOffset + valueSize;

			// Move all data (including the spare slot) to the right of
			// <splitOffset> to the new node
			System.arraycopy(data, splitOffset, newNode.data, 4, data.length - splitOffset);

			// Get the median value
			byte[] medianValue = getValue(medianIdx);

			// Clear the right half of the data in this node
			clearData(medianOffset, data.length);

			// Update the value counts
			setValueCount(medianIdx);
			newNode.setValueCount(branchFactor - medianIdx - 1);
			newNode.dataChanged = true;

			notifyNodeSplit(newNode, medianIdx);

			// Return the median value; it should be inserted into the parent node
			return medianValue;
		}

		public void mergeWithRightSibling(byte[] medianValue, Node rightSibling)
			throws IOException
		{
			// Append median value from parent node
			insertValueNodeIDPair(valueCount, medianValue, 0);
			// setValue(valueCount, medianValue);

			int rightIdx = valueCount;

			// Append all values and node references from right sibling
			System.arraycopy(rightSibling.data, 4, data, nodeIdx2offset(rightIdx),
					valueIdx2offset(rightSibling.valueCount) - 4);

			setValueCount(valueCount + rightSibling.valueCount);

			rightSibling.clearData(4, valueIdx2offset(rightSibling.valueCount));
			rightSibling.setValueCount(0);
			rightSibling.dataChanged = true;

			rightSibling.notifyNodeMerged(this, rightIdx);
		}

		public void register(NodeListener listener) {
			synchronized (listeners) {
				assert !listeners.contains(listener);
				listeners.add(listener);
			}
		}

		public void deregister(NodeListener listener) {
			synchronized (listeners) {
				assert listeners.contains(listener);
				listeners.remove(listener);
			}
		}

		private void notifyValueAdded(int index) {
			synchronized (listeners) {
				Iterator<NodeListener> iter = listeners.iterator();

				while (iter.hasNext()) {
					// Deregister if listener return true
					if (iter.next().valueAdded(this, index)) {
						iter.remove();
					}
				}
			}
		}

		private void notifyValueRemoved(int index) {
			synchronized (listeners) {
				Iterator<NodeListener> iter = listeners.iterator();

				while (iter.hasNext()) {
					// Deregister if listener return true
					if (iter.next().valueRemoved(this, index)) {
						iter.remove();
					}
				}
			}
		}

		private void notifyValueChanged(int index) {
			synchronized (listeners) {
				Iterator<NodeListener> iter = listeners.iterator();

				while (iter.hasNext()) {
					// Deregister if listener return true
					if (iter.next().valueChanged(this, index)) {
						iter.remove();
					}
				}
			}
		}

		private void notifyNodeSplit(Node rightNode, int medianIdx)
			throws IOException
		{
			synchronized (listeners) {
				Iterator<NodeListener> iter = listeners.iterator();

				while (iter.hasNext()) {
					boolean deregister = iter.next().nodeSplit(this, rightNode, medianIdx);

					if (deregister) {
						iter.remove();
					}
				}
			}
		}

		private void notifyNodeMerged(Node targetNode, int mergeIdx)
			throws IOException
		{
			synchronized (listeners) {
				Iterator<NodeListener> iter = listeners.iterator();

				while (iter.hasNext()) {
					boolean deregister = iter.next().nodeMergedWith(this, targetNode, mergeIdx);

					if (deregister) {
						iter.remove();
					}
				}
			}
		}

		public void read()
			throws IOException
		{
			ByteBuffer buf = ByteBuffer.wrap(data);
			// Don't fill the spare slot in data:
			buf.limit(nodeSize);
			fileChannel.read(buf, offset);

			valueCount = ByteArrayUtil.getInt(data, 0);
		}

		public void write()
			throws IOException
		{
			ByteBuffer buf = ByteBuffer.wrap(data);
			// Don't write the spare slot in data to the file:
			buf.limit(nodeSize);
			fileChannel.write(buf, offset);
			dataChanged = false;
		}

		/**
		 * Shifts the data between <tt>startOffset</tt> (inclusive) and
		 * <tt>endOffset</tt> (exclusive) <tt>shift</tt> positions to the
		 * right. Negative shift values can be used to shift data to the left.
		 */
		private void shiftData(int startOffset, int endOffset, int shift) {
			System.arraycopy(data, startOffset, data, startOffset + shift, endOffset - startOffset);
		}

		/**
		 * Clears the data between <tt>startOffset</tt> (inclusive) and
		 * <tt>endOffset</tt> (exclusive). All bytes in this range will be set
		 * to 0.
		 */
		private void clearData(int startOffset, int endOffset) {
			Arrays.fill(data, startOffset, endOffset, (byte)0);
		}

		private void setValueCount(int valueCount) {
			this.valueCount = valueCount;
			ByteArrayUtil.putInt(valueCount, data, 0);
		}

		private int valueIdx2offset(int id) {
			return 8 + id * slotSize;
		}

		private int nodeIdx2offset(int id) {
			return 4 + id * slotSize;
		}
	}

	/*--------------------------*
	 * Inner class NodeListener *
	 *--------------------------*/

	private interface NodeListener {

		/**
		 * Signals to registered node listeners that a value has been added to a
		 * node.
		 * 
		 * @param node
		 *        The node which the value has been added to.
		 * @param index
		 *        The index where the value was inserted.
		 * @return Indicates whether the node listener should be deregistered as a
		 *         result of this event.
		 */
		public boolean valueAdded(Node node, int index);

		/**
		 * Signals to registered node listeners that a value has been removed from
		 * a node.
		 * 
		 * @param node
		 *        The node which the value has been removed from.
		 * @param index
		 *        The index where the value was removed.
		 * @return Indicates whether the node listener should be deregistered as a
		 *         result of this event.
		 */
		public boolean valueRemoved(Node node, int index);

		/**
		 * Signals to registered node listeners that a value has been changed.
		 * 
		 * @param node
		 *        The node in which the value has been changed.
		 * @param index
		 *        The index of the changed value.
		 * @return Indicates whether the node listener should be deregistered as a
		 *         result of this event.
		 */
		public boolean valueChanged(Node node, int index);

		/**
		 * Signals to registered node listeners that a node has been split.
		 * 
		 * @param node
		 *        The node which has been split.
		 * @param newNode
		 *        The newly allocated node containing the "right" half of the
		 *        values.
		 * @param medianIdx
		 *        The index where the node has been split. The value at this index
		 *        has been moved to the node's parent.
		 * @return Indicates whether the node listener should be deregistered as a
		 *         result of this event.
		 */
		public boolean nodeSplit(Node node, Node newNode, int medianIdx)
			throws IOException;

		/**
		 * Signals to registered node listeners that two nodes have been merged.
		 * All values from the source node have been appended to the value of the
		 * target node.
		 * 
		 * @param sourceNode
		 *        The node that donated its values to the target node.
		 * @param targetNode
		 *        The node in which the values have been merged.
		 * @param mergeIdx
		 *        The index of <tt>sourceNode</tt>'s values in
		 *        <tt>targetNode</tt>.
		 * 
		 * @return Indicates whether the node listener should be deregistered with
		 *         the <em>source node</em> as a result of this event.
		 */
		public boolean nodeMergedWith(Node sourceNode, Node targetNode, int mergeIdx)
			throws IOException;
	}

	/*-----------------------------*
	 * Inner class SeqScanIterator *
	 *-----------------------------*/

	// private class SeqScanIterator implements RecordIterator {
	//
	// private byte[] searchKey;
	//
	// private byte[] searchMask;
	//
	// private int currentNodeID;
	//
	// private Node currentNode;
	//
	// private int currentIdx;
	//
	// public SeqScanIterator(byte[] searchKey, byte[] searchMask) {
	// this.searchKey = searchKey;
	// this.searchMask = searchMask;
	// }
	//
	// public byte[] next()
	// throws IOException
	// {
	// while (currentNodeID <= maxNodeID) {
	// if (currentNode == null) {
	// // Read first node
	// currentNodeID = 1;
	// currentNode = readNode(currentNodeID);
	// currentIdx = 0;
	// }
	//
	// while (currentIdx < currentNode.getValueCount()) {
	// byte[] value = currentNode.getValue(currentIdx++);
	//
	// if (searchKey == null || ByteArrayUtil.matchesPattern(value, searchMask,
	// searchKey)) {
	// // Found a matches value
	// return value;
	// }
	// }
	//
	// currentNode.release();
	//
	// currentNodeID++;
	// currentNode = (currentNodeID <= maxNodeID) ? readNode(currentNodeID) :
	// null;
	// currentIdx = 0;
	// }
	//
	// return null;
	// }
	//
	// public void set(byte[] value) {
	// if (currentNode == null || currentIdx > currentNode.getValueCount()) {
	// throw new IllegalStateException();
	// }
	//
	// currentNode.setValue(currentIdx - 1, value);
	// }
	//
	// public void close()
	// throws IOException
	// {
	// if (currentNode != null) {
	// currentNodeID = maxNodeID + 1;
	//
	// currentNode.release();
	// currentNode = null;
	// }
	// }
	// }
	/*---------------------------*
	 * Inner class RangeIterator *
	 *---------------------------*/

	private class RangeIterator implements RecordIterator, NodeListener {

		private byte[] searchKey;

		private byte[] searchMask;

		private byte[] minValue;

		private byte[] maxValue;

		private boolean started;

		private Node currentNode;

		/**
		 * Tracks the parent nodes of {@link #currentNode}.
		 */
		private LinkedList<Node> parentNodeStack = new LinkedList<Node>();

		/**
		 * Tracks the index of child nodes in parent nodes.
		 */
		private LinkedList<Integer> parentIndexStack = new LinkedList<Integer>();

		private int currentIdx;

		public RangeIterator(byte[] searchKey, byte[] searchMask, byte[] minValue, byte[] maxValue) {
			this.searchKey = searchKey;
			this.searchMask = searchMask;
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.started = false;
		}

		public byte[] next()
			throws IOException
		{
			if (!started) {
				started = true;
				findMinimum();
			}

			byte[] value = findNext(false);
			while (value != null) {
				if (maxValue != null && comparator.compareBTreeValues(maxValue, value, 0, value.length) < 0) {
					// Reached maximum value, stop iterating
					close();
					value = null;
					break;
				}
				else if (searchKey != null && !ByteArrayUtil.matchesPattern(value, searchMask, searchKey)) {
					// Value doesn't match search key/mask
					value = findNext(false);
					continue;
				}
				else {
					// Matching value found
					break;
				}
			}

			return value;
		}

		private void findMinimum()
			throws IOException
		{
			if (rootNodeID == 0) {
				// Empty BTree
				return;
			}

			currentNode = readNode(rootNodeID);
			currentNode.register(this);
			currentIdx = 0;

			// Search first value >= minValue, or the left-most value in case
			// minValue is null
			while (true) {
				if (minValue != null) {
					currentIdx = currentNode.search(minValue);

					if (currentIdx >= 0) {
						// Found exact match with minimum value
						break;
					}
					else {
						// currentIdx indicates the first value larger than the
						// minimum value
						currentIdx = -currentIdx - 1;
					}
				}

				if (currentNode.isLeaf()) {
					break;
				}
				else {
					parentNodeStack.add(currentNode);
					parentIndexStack.add(currentIdx);

					currentNode = currentNode.getChildNode(currentIdx);
					currentNode.register(this);
					currentIdx = 0;
				}
			}
		}

		private byte[] findNext(boolean returnedFromRecursion)
			throws IOException
		{
			if (currentNode == null) {
				return null;
			}

			if (returnedFromRecursion || currentNode.isLeaf()) {
				if (currentIdx >= currentNode.getValueCount()) {
					// No more values in this node, continue with parent node
					currentNode.deregister(this);
					currentNode.release();
					currentNode = popNodeStack();
					currentIdx = popIndexStack();
					return findNext(true);
				}
				else {
					return currentNode.getValue(currentIdx++);
				}
			}
			else {
				parentNodeStack.add(currentNode);
				parentIndexStack.add(currentIdx);

				currentNode = currentNode.getChildNode(currentIdx);
				currentNode.register(this);
				currentIdx = 0;

				return findNext(false);
			}
		}

		public void set(byte[] value) {
			if (currentNode == null || currentIdx > currentNode.getValueCount()) {
				throw new IllegalStateException();
			}

			currentNode.setValue(currentIdx - 1, value);
		}

		public void close()
			throws IOException
		{
			while (currentNode != null) {
				currentNode.deregister(this);
				currentNode.release();
				currentNode = popNodeStack();
			}

			assert parentNodeStack.isEmpty();
			parentIndexStack.clear();
		}

		private Node popNodeStack() {
			if (!parentNodeStack.isEmpty()) {
				return parentNodeStack.removeLast();
			}

			return null;
		}

		private int popIndexStack() {
			if (!parentIndexStack.isEmpty()) {
				return parentIndexStack.removeLast();
			}

			return 0;
		}

		public boolean valueAdded(Node node, int index) {
			if (node == currentNode) {
				if (index <= currentIdx) {
					currentIdx++;
				}
			}
			else {
				for (int i = 0; i < parentNodeStack.size(); i++) {
					if (node == parentNodeStack.get(i)) {
						if (index <= parentIndexStack.get(i)) {
							parentIndexStack.set(i, index + 1);
						}

						break;
					}
				}
			}

			return false;
		}

		public boolean valueRemoved(Node node, int index) {
			if (node == currentNode) {
				if (index <= currentIdx) {
					currentIdx--;
				}
			}
			else {
				for (int i = 0; i < parentNodeStack.size(); i++) {
					if (node == parentNodeStack.get(i)) {
						if (index <= parentIndexStack.get(i)) {
							parentIndexStack.set(i, index - 1);
						}

						break;
					}
				}
			}

			return false;
		}

		public boolean valueChanged(Node node, int index) {
			return false;
		}

		public boolean nodeSplit(Node node, Node newNode, int medianIdx)
			throws IOException
		{
			boolean deregister = false;

			if (node == currentNode) {
				if (currentIdx > medianIdx) {
					currentNode.release();
					deregister = true;

					newNode.use();
					newNode.register(this);

					currentNode = newNode;
					currentIdx -= medianIdx + 1;
				}
			}
			else {
				for (int i = 0; i < parentNodeStack.size(); i++) {
					Node parentNode = parentNodeStack.get(i);

					if (node == parentNode) {
						int parentIdx = parentIndexStack.get(i);

						if (parentIdx > medianIdx) {
							parentNode.release();
							deregister = true;

							newNode.use();
							newNode.register(this);

							parentNodeStack.set(i, newNode);
							parentIndexStack.set(i, parentIdx - medianIdx - 1);
						}

						break;
					}
				}
			}

			return deregister;
		}

		public boolean nodeMergedWith(Node sourceNode, Node targetNode, int mergeIdx)
			throws IOException
		{
			boolean deregister = false;

			if (sourceNode == currentNode) {
				currentNode.release();
				deregister = true;

				targetNode.use();
				targetNode.register(this);

				currentNode = targetNode;
				currentIdx += mergeIdx;
			}
			else {
				for (int i = 0; i < parentNodeStack.size(); i++) {
					Node parentNode = parentNodeStack.get(i);

					if (sourceNode == parentNode) {
						parentNode.release();
						deregister = true;

						targetNode.use();
						targetNode.register(this);

						parentNodeStack.set(i, targetNode);
						parentIndexStack.set(i, mergeIdx + parentIndexStack.get(i));

						break;
					}
				}
			}

			return deregister;
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
		RecordComparator comparator = new DefaultRecordComparator();
		BTree btree = new BTree(dataFile, 501, 13, comparator);

		java.util.Random random = new java.util.Random(0L);
		byte[] value = new byte[13];

		long startTime = System.currentTimeMillis();
		for (int i = 1; i <= valueCount; i++) {
			random.nextBytes(value);
			btree.insert(value);
			if (i % 50000 == 0) {
				System.out.println("Inserted " + i + " values in " + (System.currentTimeMillis() - startTime)
						+ " ms");
			}
		}

		System.out.println("Iterating over all values in sequential order...");
		startTime = System.currentTimeMillis();
		RecordIterator iter = btree.iterateAll();
		value = iter.next();
		int count = 0;
		while (value != null) {
			count++;
			value = iter.next();
		}
		iter.close();
		System.out.println("Iteration over " + count + " items finished in "
				+ (System.currentTimeMillis() - startTime) + " ms");

		// byte[][] values = new byte[count][13];
		//
		// iter = btree.iterateAll();
		// for (int i = 0; i < values.length; i++) {
		// values[i] = iter.next();
		// }
		// iter.close();
		//
		// startTime = System.currentTimeMillis();
		// for (int i = values.length - 1; i >= 0; i--) {
		// btree.remove(values[i]);
		// }
		// System.out.println("Removed all item in " + (System.currentTimeMillis()
		// - startTime) + " ms");
	}

	public static void runDebugTest(String[] args)
		throws Exception
	{
		File dataFile = new File(args[0]);
		BTree btree = new BTree(dataFile, 28, 1);

		btree.print(System.out);

		/*
		 * System.out.println("Adding values..."); btree.startTransaction();
		 * btree.insert("C".getBytes()); btree.insert("N".getBytes());
		 * btree.insert("G".getBytes()); btree.insert("A".getBytes());
		 * btree.insert("H".getBytes()); btree.insert("E".getBytes());
		 * btree.insert("K".getBytes()); btree.insert("Q".getBytes());
		 * btree.insert("M".getBytes()); btree.insert("F".getBytes());
		 * btree.insert("W".getBytes()); btree.insert("L".getBytes());
		 * btree.insert("T".getBytes()); btree.insert("Z".getBytes());
		 * btree.insert("D".getBytes()); btree.insert("P".getBytes());
		 * btree.insert("R".getBytes()); btree.insert("X".getBytes());
		 * btree.insert("Y".getBytes()); btree.insert("S".getBytes());
		 * btree.commitTransaction(); btree.print(System.out);
		 * System.out.println("Removing values..."); System.out.println("Removing
		 * H..."); btree.remove("H".getBytes()); btree.commitTransaction();
		 * btree.print(System.out); System.out.println("Removing T...");
		 * btree.remove("T".getBytes()); btree.commitTransaction();
		 * btree.print(System.out); System.out.println("Removing R...");
		 * btree.remove("R".getBytes()); btree.commitTransaction();
		 * btree.print(System.out); System.out.println("Removing E...");
		 * btree.remove("E".getBytes()); btree.commitTransaction();
		 * btree.print(System.out); System.out.println("Values from I to U:");
		 * RecordIterator iter = btree.iterateRange("I".getBytes(),
		 * "V".getBytes()); byte[] value = iter.next(); while (value != null) {
		 * System.out.print(new String(value) + " "); value = iter.next(); }
		 * System.out.println();
		 */
	}

	public void print(PrintStream out)
		throws IOException
	{
		out.println("---contents of BTree file---");
		out.println("Stored parameters:");
		out.println("block size   = " + blockSize);
		out.println("value size   = " + valueSize);
		out.println("root node ID = " + rootNodeID);
		out.println();
		out.println("Derived parameters:");
		out.println("slot size       = " + slotSize);
		out.println("branch factor   = " + branchFactor);
		out.println("min value count = " + minValueCount);
		out.println("node size       = " + nodeSize);
		out.println("max node ID     = " + maxNodeID);
		out.println();

		ByteBuffer buf = ByteBuffer.allocate(nodeSize);
		for (long offset = blockSize; offset < fileChannel.size(); offset += blockSize) {
			fileChannel.read(buf, offset);
			buf.rewind();

			int nodeID = offset2nodeID(offset);
			int count = buf.getInt();
			out.print("node " + nodeID + ": ");
			out.print("count=" + count + " ");

			byte[] value = new byte[valueSize];

			for (int i = 0; i < count; i++) {
				// node ID
				out.print(buf.getInt());

				// value
				buf.get(value);
				out.print("[" + ByteArrayUtil.toHexString(value) + "]");
				// out.print("["+new String(value)+"]");
			}

			// last node ID
			out.println(buf.getInt());

			buf.clear();
		}
		out.println("---end of BTree file---");
	}
}
