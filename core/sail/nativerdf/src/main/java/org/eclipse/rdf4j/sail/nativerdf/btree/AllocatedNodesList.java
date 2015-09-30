/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.nativerdf.btree;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;

import org.eclipse.rdf4j.common.io.ByteArrayUtil;
import org.eclipse.rdf4j.common.io.NioFile;

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
	private final NioFile nioFile;

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

		this.nioFile = new NioFile(allocNodesFile);
		this.btree = btree;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the allocated nodes file.
	 */
	public File getFile() {
		return nioFile.getFile();
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
		return nioFile.delete();
	}

	public synchronized void close(boolean syncChanges)
		throws IOException
	{
		if (syncChanges) {
			sync();
		}
		allocatedNodes = null;
		needsSync = false;
		nioFile.close();
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
			nioFile.truncate(HEADER_LENGTH + data.length);
			nioFile.writeBytes(MAGIC_NUMBER, 0);
			nioFile.writeByte(FILE_FORMAT_VERSION, MAGIC_NUMBER.length);
			nioFile.writeBytes(data, HEADER_LENGTH);

			needsSync = false;
		}
	}

	private void scheduleSync()
		throws IOException
	{
		if (needsSync == false) {
			nioFile.truncate(0);
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
			if (nioFile.size() > 0L) {
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
		byte[] data;

		if (nioFile.size() >= HEADER_LENGTH
				&& Arrays.equals(MAGIC_NUMBER, nioFile.readBytes(0, MAGIC_NUMBER.length)))
		{
			byte version = nioFile.readByte(MAGIC_NUMBER.length);
			if (version > FILE_FORMAT_VERSION) {
				throw new IOException("Unable to read allocated nodes file; it uses a newer file format");
			}
			else if (version != FILE_FORMAT_VERSION) {
				throw new IOException("Unable to read allocated nodes file; invalid file format version: "
						+ version);
			}

			data = nioFile.readBytes(HEADER_LENGTH, (int)(nioFile.size() - HEADER_LENGTH));
		}
		else {
			// assume header is missing (old file format)
			data = nioFile.readBytes(0, (int)nioFile.size());
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
