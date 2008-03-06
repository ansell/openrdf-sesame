/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.datastore;

import java.io.File;
import java.io.IOException;

import info.aduna.io.ByteArrayUtil;

import org.openrdf.sail.nativerdf.btree.BTree;
import org.openrdf.sail.nativerdf.btree.BTreeIterator;
import org.openrdf.sail.nativerdf.btree.DefaultBTreeValueComparator;


/**
 * A file-based hash table based on B-Trees.
 */
public class HashIndex {

	/*-----------*
	 * Constants *
	 *-----------*/

	/* Records consist of 8 bytes:
     *  byte  0-3 : hash code
     *  byte  4-7 : ID
	 */
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
	private File _file;

	/**
	 * The BTree that is used to store the hash code records.
	 */
	private BTree _btree;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public HashIndex(File file)
		throws IOException
	{
		_file = file;
		_btree = new BTree(_file, 4096, RECORD_LENGTH,
				new DefaultBTreeValueComparator());
	}

	/*---------*
	 * Methods *
	 *---------*/

	public File getFile() {
		return _file;
	}

	/**
	 * Gets an iterator that iterates over the IDs with hash codes that match
	 * the specified hash code.
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
		_btree.insert(_getData(hash, id));
	}

	/**
	 * Removes the specified ID from this hash index. 
	 */
	public void removeID(int hash, int id)
		throws IOException
	{
		_btree.remove(_getData(hash, id));
	}

	public void sync()
		throws IOException
	{
		_btree.sync();
	}

	public void clear()
		throws IOException
	{
		_btree.clear();
	}

	public void close()
		throws IOException
	{
		_btree.close();
	}

	private byte[] _getData(int hash, int id) {
		byte[] data = new byte[RECORD_LENGTH];
		ByteArrayUtil.putInt(hash, data, HASH_IDX);
		ByteArrayUtil.putInt(id, data, ID_IDX);
		return data;
	}

	private byte[] _getMinValue(int hash) {
		byte[] minValue = new byte[RECORD_LENGTH];
		ByteArrayUtil.putInt(hash, minValue, HASH_IDX);
		return minValue;
	}

	private byte[] _getMaxValue(int hash) {
		byte[] maxValue = new byte[RECORD_LENGTH];
		ByteArrayUtil.putInt(hash, maxValue, HASH_IDX);
		ByteArrayUtil.putInt(0xffffffff, maxValue, ID_IDX);
		return maxValue;
	}

	/*------------------------*
	 * Inner class IDIterator *
	 *------------------------*/

	public class IDIterator {
	
		private BTreeIterator _btreeIter;
	
		private IDIterator(int hash)
			throws IOException
		{
			byte[] minValue = _getMinValue(hash);
			byte[] maxValue = _getMaxValue(hash);

			_btreeIter = _btree.iterateRange(minValue, maxValue);
		}
	
		/**
		 * Returns the next ID that has been mapped to the specified hash code,
		 * or <tt>-1</tt> if no more IDs were found.
		 */
		public int next()
			throws IOException
		{
			byte[] result = _btreeIter.next();

			if (result == null) {
				return -1;
			}

			return ByteArrayUtil.getInt(result, ID_IDX);
		}
		
		public void close()
			throws IOException	
		{
			_btreeIter.close();
		}
	} // End inner class IDIterator
}
