/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.btree;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;

import info.aduna.io.ByteArrayUtil;

/**
 * List of allocated BTree nodes, persisted to a file on disk.
 * 
 * @author Arjohn Kampman
 */
class AllocatedNodesList {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * Magic number "Allocated Nodes File" to detect whether the file is actually
	 * an allocated nodes file. The first three bytes of the file should be equal
	 * to this magic number.
	 */
	private static final byte[] MAGIC_NUMBER = new byte[] { 'a', 'n', 'f' };

	/**
	 * The file format version number, stored as the fourth byte in allocated
	 * nodes files.
	 */
	private static final byte FILE_FORMAT_VERSION = 1;

	private static final int HEADER_LENGTH = MAGIC_NUMBER.length + 1;

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The BTree associated with this allocated nodes list.
	 */
	private final BTree btree;

	/**
	 * The allocated nodes file.
	 */
	private final File allocNodesFile;

	private final RandomAccessFile raf;

	/**
	 * Bit set recording which nodes have been allocated, using node IDs as
	 * index.
	 */
	private BitSet allocatedNodes;

	/**
	 * Flag indicating whether the set of allocated nodes has changed and needs
	 * to be written to file.
	 */
	private boolean needsSync = false;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new AllocatedNodelist for the specified BTree.
	 */
	public AllocatedNodesList(File allocNodesFile, BTree btree)
		throws IOException
	{
		if (allocNodesFile == null) {
			throw new IllegalArgumentException("allocNodesFile must not be null");
		}
		if (btree == null) {
			throw new IllegalArgumentException("btree muts not be null");
		}

		this.allocNodesFile = allocNodesFile;
		this.raf = new RandomAccessFile(allocNodesFile, "rw");
		this.btree = btree;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the allocated nodes file.
	 */
	public File getFile() {
		return allocNodesFile;
	}

	public synchronized void close()
		throws IOException
	{
		close(true);
	}

	/**
	 * Deletes the allocated nodes file.
	 * 
	 * @return <tt>true</tt> if the file was deleted.
	 */
	public synchronized boolean delete()
		throws IOException
	{
		close(false);
		return allocNodesFile.delete();
	}

	public synchronized void close(boolean syncChanges)
		throws IOException
	{
		if (syncChanges) {
			sync();
		}
		allocatedNodes = null;
		needsSync = false;
		raf.close();
	}

	/**
	 * Writes any changes that are cached in memory to disk.
	 * 
	 * @throws IOException
	 */
	public synchronized void sync()
		throws IOException
	{
		if (needsSync) {
			// Trim bit set
			BitSet bitSet = allocatedNodes;
			int bitSetLength = allocatedNodes.length();
			if (bitSetLength < allocatedNodes.size()) {
				bitSet = allocatedNodes.get(0, bitSetLength);
			}

			byte[] data = ByteArrayUtil.toByteArray(bitSet);

			// Write bit set to file
			raf.setLength(0L);
			raf.write(MAGIC_NUMBER);
			raf.write(FILE_FORMAT_VERSION);
			raf.write(data);

			needsSync = false;
		}
	}

	private void scheduleSync()
		throws IOException
	{
		if (needsSync == false) {
			raf.setLength(0L);
			needsSync = true;
		}
	}

	/**
	 * Clears the allocated nodes list.
	 * 
	 * @throws IOException
	 *         If an I/O error occurred.
	 */
	public synchronized void clear()
		throws IOException
	{
		if (allocatedNodes != null) {
			allocatedNodes.clear();
		}
		else {
			// bit set has not yet been initialized
			allocatedNodes = new BitSet();
		}

		scheduleSync();
	}

	public synchronized int allocateNode()
		throws IOException
	{
		initAllocatedNodes();

		int newNodeID = allocatedNodes.nextClearBit(1);
		allocatedNodes.set(newNodeID);

		scheduleSync();

		return newNodeID;
	}

	public synchronized void freeNode(int nodeID)
		throws IOException
	{
		initAllocatedNodes();
		allocatedNodes.clear(nodeID);
		scheduleSync();
	}

	/**
	 * Returns the highest allocated node ID.
	 */
	public synchronized int getMaxNodeID()
		throws IOException
	{
		initAllocatedNodes();
		return Math.max(0, allocatedNodes.length() - 1);
	}

	/**
	 * Returns the number of allocated nodes.
	 */
	public synchronized int getNodeCount()
		throws IOException
	{
		initAllocatedNodes();
		return allocatedNodes.cardinality();
	}

	private void initAllocatedNodes()
		throws IOException
	{
		if (allocatedNodes == null) {
			if (raf.length() > 0L) {
				loadAllocatedNodesInfo();
			}
			else {
				crawlAllocatedNodes();
			}
		}
	}

	private void loadAllocatedNodesInfo()
		throws IOException
	{
		byte[] data = new byte[(int)raf.length()];
		raf.seek(0L);
		raf.readFully(data);

		if (data.length >= HEADER_LENGTH && ByteArrayUtil.regionMatches(MAGIC_NUMBER, data, 0)) {
			byte version = data[MAGIC_NUMBER.length];
			if (version > FILE_FORMAT_VERSION) {
				throw new IOException("Unable to read allocated nodes file; it uses a newer file format");
			}
			else if (version != FILE_FORMAT_VERSION) {
				throw new IOException("Unable to read allocated nodes file; invalid file format version: "
						+ version);
			}

			// Remove the header from the data array
			data = ByteArrayUtil.get(data, HEADER_LENGTH);
		}
		else {
			// assume header is missing (old file format)
			scheduleSync();
		}

		allocatedNodes = ByteArrayUtil.toBitSet(data);
	}

	private void crawlAllocatedNodes()
		throws IOException
	{
		allocatedNodes = new BitSet();

		BTree.Node rootNode = btree.readRootNode();
		if (rootNode != null) {
			crawlAllocatedNodes(rootNode);
		}

		scheduleSync();
	}

	private void crawlAllocatedNodes(BTree.Node node)
		throws IOException
	{
		try {
			allocatedNodes.set(node.getID());

			if (!node.isLeaf()) {
				for (int i = 0; i < node.getValueCount() + 1; i++) {
					crawlAllocatedNodes(node.getChildNode(i));
				}
			}

		}
		finally {
			node.release();
		}
	}
}