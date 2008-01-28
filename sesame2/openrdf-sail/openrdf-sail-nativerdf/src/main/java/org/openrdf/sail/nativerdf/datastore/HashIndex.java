/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.datastore;

import java.io.File;
import java.io.IOException;

import info.aduna.io.ByteArrayUtil;

import org.openrdf.sail.nativerdf.btree.BTree;
import org.openrdf.sail.nativerdf.btree.RecordIterator;
import org.openrdf.sail.nativerdf.btree.DefaultRecordComparator;

/**
 * A file-based hash table based on B-Trees.
 * 
 * @author Arjohn Kampman
 */
public class HashIndex {

	/*-----------*
	 * Constants *
	 *-----------*/

	// Records consist of 8 bytes:
	// byte 0-3 : hash code
	// byte 4-7 : ID
	private static final int RECORD_LENGTH = 8;

	private static final int HASH_IDX = 0;

	private static final int ID_IDX = 4;

	private static final byte[] SEARCH_MASK = new byte[RECORD_LENGTH];

	static {
		ByteArrayUtil.putInt(0xffffffff, SEARCH_MASK, HASH_IDX);
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The file that is used to store the hash index.
	 */
	private File file;

	/**
	 * The BTree that is used to store the hash code records.
	 */
	private BTree btree;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public HashIndex(File file)
		throws IOException
	{
		this.file = file;
		btree = new BTree(file, 4096, RECORD_LENGTH, new DefaultRecordComparator());
	}

	/*---------*
	 * Methods *
	 *---------*/

	public File getFile() {
		return file;
	}

	/**
	 * Gets an iterator that iterates over the IDs with hash codes that match the
	 * specified hash code.
	 */
	public IDIterator getIDIterator(int hash)
		throws IOException
	{
		return new IDIterator(hash);
	}

	/**
	 * Stores an ID under the specified hash code in this hash index.
	 */
	public void storeID(int hash, int id)
		throws IOException
	{
		btree.insert(getData(hash, id));
	}

	/**
	 * Removes the specified ID from this hash index.
	 */
	public void removeID(int hash, int id)
		throws IOException
	{
		btree.remove(getData(hash, id));
	}

	public void sync()
		throws IOException
	{
		btree.sync();
	}

	public void clear()
		throws IOException
	{
		btree.clear();
	}

	public void close()
		throws IOException
	{
		btree.close();
	}

	private byte[] getData(int hash, int id) {
		byte[] data = new byte[RECORD_LENGTH];
		ByteArrayUtil.putInt(hash, data, HASH_IDX);
		ByteArrayUtil.putInt(id, data, ID_IDX);
		return data;
	}

	private byte[] getMinValue(int hash) {
		byte[] minValue = new byte[RECORD_LENGTH];
		ByteArrayUtil.putInt(hash, minValue, HASH_IDX);
		return minValue;
	}

	private byte[] getMaxValue(int hash) {
		byte[] maxValue = new byte[RECORD_LENGTH];
		ByteArrayUtil.putInt(hash, maxValue, HASH_IDX);
		ByteArrayUtil.putInt(0xffffffff, maxValue, ID_IDX);
		return maxValue;
	}

	/*------------------------*
	 * Inner class IDIterator *
	 *------------------------*/

	public class IDIterator {

		private RecordIterator btreeIter;

		private IDIterator(int hash)
			throws IOException
		{
			byte[] minValue = getMinValue(hash);
			byte[] maxValue = getMaxValue(hash);

			btreeIter = btree.iterateRange(minValue, maxValue);
		}

		/**
		 * Returns the next ID that has been mapped to the specified hash code, or
		 * <tt>-1</tt> if no more IDs were found.
		 */
		public int next()
			throws IOException
		{
			byte[] result = btreeIter.next();

			if (result == null) {
				return -1;
			}

			return ByteArrayUtil.getInt(result, ID_IDX);
		}

		public void close()
			throws IOException
		{
			btreeIter.close();
		}
	} // End inner class IDIterator
}
