/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.datastore;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.CRC32;

import info.aduna.io.ByteArrayUtil;


/**
 * Class that provides indexed storage and retrieval of arbitrary length data.
 */
public class DataStore {

	/*-----------*
	 * Variables *
	 *-----------*/

	private DataFile _dataFile;

	private IDFile _idFile;

	private HashFile _hashFile;

	/**
	 * The checksum to use for calculating data hashes.
	 */
	private CRC32 _crc32 = new CRC32();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public DataStore(File dataDir, String filePrefix)
		throws IOException
	{
		_dataFile = new DataFile(new File(dataDir, filePrefix + ".dat"));
		_idFile = new IDFile(new File(dataDir, filePrefix + ".id"));
		_hashFile = new HashFile(new File(dataDir, filePrefix + ".hash"));
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the value for the specified ID.
	 * 
	 * @param id
	 *        A value ID, should be larger than 0.
	 * @return The value for the ID, or <tt>null</tt> if no such value could be
	 *         found.
	 * @exception IOException
	 *            If an I/O error occurred.
	 */
	public byte[] getData(int id)
		throws IOException
	{
		assert id > 0 : "id must be larger than 0, is: " + id;

		// Data not in cache or cache not used, fetch from file
		long offset = _idFile.getOffset(id);

		if (offset != 0L) {
			return _dataFile.getData(offset);
		}

		return null;
	}

	/**
	 * Gets the ID for the specified value.
	 * 
	 * @param queryData
	 *        The value to get the ID for, must not be <tt>null</tt>.
	 * @return The ID for the specified value, or <tt>-1</tt> if no such ID
	 *         could be found.
	 * @exception IOException
	 *            If an I/O error occurred.
	 */
	public int getID(byte[] queryData)
		throws IOException
	{
		assert queryData != null : "queryData must not be null";

		int id = -1;

		// Value not in cache or cache not used, fetch from file
		int hash = _getDataHash(queryData);
		HashFile.IDIterator iter = _hashFile.getIDIterator(hash);

		while ((id = iter.next()) >= 0) {
			long offset = _idFile.getOffset(id);
			byte[] data = _dataFile.getData(offset);

			if (Arrays.equals(queryData, data)) {
				// Matching data found
				break;
			}
		}

		return id;
	}

	/**
	 * Returns the maximum value-ID that is in use.
	 * 
	 * @return The largest ID, or <tt>0</tt> if the store does not contain any
	 *         values.
	 * @throws IOException
	 *         If an I/O error occurs.
	 */
	public int getMaxID()
		throws IOException
	{
		return _idFile.getMaxID();
	}

	/**
	 * Stores the supplied value and returns the ID that has been assigned to it.
	 * In case the data to store is already present, the ID of this existing data
	 * is returned.
	 * 
	 * @param data
	 *        The data to store, must not be <tt>null</tt>.
	 * @return The ID that has been assigned to the value.
	 * @exception IOException
	 *            If an I/O error occurred.
	 */
	public int storeData(byte[] data)
		throws IOException
	{
		assert data != null : "data must not be null";

		int id = getID(data);

		if (id == -1) {
			// Data not stored yet, store it under a new ID.
			long offset = _dataFile.storeData(data);
			id = _idFile.storeOffset(offset);
			_hashFile.storeID(_getDataHash(data), id);
		}

		return id;
	}

	/**
	 * Synchronizes any recent changes to the data to disk.
	 * 
	 * @exception IOException
	 *            If an I/O error occurred.
	 */
	public void sync()
		throws IOException
	{
		_hashFile.sync();
		_idFile.sync();
		_dataFile.sync();
	}

	/**
	 * Removes all values from the DataStore.
	 * 
	 * @exception IOException
	 *            If an I/O error occurred.
	 */
	public void clear()
		throws IOException
	{
		_hashFile.clear();
		_idFile.clear();
		_dataFile.clear();
	}

	/**
	 * Closes the DataStore, releasing any file references, etc. In case a
	 * transaction is currently open, it will be rolled back. Once closed, the
	 * DataStore can no longer be used.
	 * 
	 * @exception IOException
	 *            If an I/O error occurred.
	 */
	public void close()
		throws IOException
	{
		_hashFile.close();
		_idFile.close();
		_dataFile.close();
	}

	/**
	 * Gets a hash code for the supplied data.
	 * 
	 * @param data
	 *        The data to calculate the hash code for.
	 * @return A hash code for the supplied data.
	 */
	private int _getDataHash(byte[] data) {
		synchronized (_crc32) {
			_crc32.update(data);
			int crc = (int)_crc32.getValue();
			_crc32.reset();
			return crc;
		}
	}

	/*--------------------*
	 * Test/debug methods *
	 *--------------------*/

	public static void main(String[] args)
		throws Exception
	{
		if (args.length < 2) {
			System.err.println("Usage: java org.openrdf.sesame.sailimpl.nativerdf.datastore.DataStore <data-dir> <file-prefix>");
			return;
		}

		System.out.println("Dumping DataStore contents...");
		File dataDir = new File(args[0]);
		DataStore dataStore = new DataStore(dataDir, args[1]);

		DataFile.DataIterator iter = dataStore._dataFile.iterator();
		while (iter.hasNext()) {
			byte[] data = iter.next();

			System.out.println(ByteArrayUtil.toHexString(data));
		}
	}
}
