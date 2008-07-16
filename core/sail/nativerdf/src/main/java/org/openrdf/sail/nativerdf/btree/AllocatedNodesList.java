/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.btree;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;

import info.aduna.io.ByteArrayUtil;
import info.aduna.io.IOUtil;

/**
 * List of allocated BTree nodes, persisted to a file on disk.
 *
 * @author Arjohn Kampman
 */
public class AllocatedNodesList {

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
	public AllocatedNodesList(File allocNodesFile, BTree btree) {
		if (allocNodesFile == null) {
			throw new IllegalArgumentException("allocNodesFile must not be null");
		}
		if (btree == null) {
			throw new IllegalArgumentException("btree muts not be null");
		}

		this.allocNodesFile = allocNodesFile;
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

	/**
	 * Deletes the allocated nodes file.
	 * 
	 * @return <tt>true</tt> if the file was deleted.
	 */
	public boolean delete()
		throws IOException
	{
		allocatedNodes = null;
		needsSync = false;
		return allocNodesFile.delete();
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

			// Write bit set to file
			byte[] data = ByteArrayUtil.toByteArray(bitSet);
			IOUtil.writeBytes(data, allocNodesFile);
			needsSync = false;
		}
	}

	private void scheduleSync()
		throws IOException
	{
		if (needsSync == false) {
			if (allocNodesFile.exists()) {
				boolean success = allocNodesFile.delete();
				if (!success) {
					throw new IOException("Failed to delete " + allocateNode());
				}
			}
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
		allocatedNodes.clear();
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
			if (allocNodesFile.exists()) {
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
		byte[] data = IOUtil.readBytes(allocNodesFile);
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