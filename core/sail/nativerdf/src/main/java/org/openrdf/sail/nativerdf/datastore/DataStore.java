/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.nativerdf.datastore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.CRC32;

import info.aduna.io.ByteArrayUtil;

/**
 * Class that provides indexed storage and retrieval of arbitrary length data.
 * 
 * @author Arjohn Kampman
 */
public class DataStore {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final DataFile dataFile;

	private final IDFile idFile;

	private final HashFile hashFile;

	/**
	 * The checksum to use for calculating data hashes.
	 */
	private final CRC32 crc32 = new CRC32();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public DataStore(Path dataDir, String filePrefix)
		throws IOException
	{
		this(dataDir, filePrefix, false);
	}

	public DataStore(Path dataDir, String filePrefix, boolean forceSync)
		throws IOException
	{
		dataFile = new DataFile(dataDir.resolve(filePrefix + ".dat"), forceSync);
		idFile = new IDFile(dataDir.resolve(filePrefix + ".id"), forceSync);
		hashFile = new HashFile(dataDir.resolve(filePrefix + ".hash"), forceSync);
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
		long offset = idFile.getOffset(id);

		if (offset != 0L) {
			return dataFile.getData(offset);
		}

		return null;
	}

	/**
	 * Gets the ID for the specified value.
	 * 
	 * @param queryData
	 *        The value to get the ID for, must not be <tt>null</tt>.
	 * @return The ID for the specified value, or <tt>-1</tt> if no such ID could
	 *         be found.
	 * @exception IOException
	 *            If an I/O error occurred.
	 */
	public int getID(byte[] queryData)
		throws IOException
	{
		assert queryData != null : "queryData must not be null";

		int id = -1;

		// Value not in cache or cache not used, fetch from file
		int hash = getDataHash(queryData);
		HashFile.IDIterator iter = hashFile.getIDIterator(hash);
		try {
			while ((id = iter.next()) >= 0) {
				long offset = idFile.getOffset(id);
				byte[] data = dataFile.getData(offset);

				if (Arrays.equals(queryData, data)) {
					// Matching data found
					break;
				}
			}
		}
		finally {
			iter.close();
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
		return idFile.getMaxID();
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
			long offset = dataFile.storeData(data);
			id = idFile.storeOffset(offset);
			hashFile.storeID(getDataHash(data), id);
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
		hashFile.sync();
		idFile.sync();
		dataFile.sync();
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
		hashFile.clear();
		idFile.clear();
		dataFile.clear();
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
		hashFile.close();
		idFile.close();
		dataFile.close();
	}

	/**
	 * Gets a hash code for the supplied data.
	 * 
	 * @param data
	 *        The data to calculate the hash code for.
	 * @return A hash code for the supplied data.
	 */
	private int getDataHash(byte[] data) {
		synchronized (crc32) {
			crc32.update(data);
			int crc = (int)crc32.getValue();
			crc32.reset();
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

		DataFile.DataIterator iter = dataStore.dataFile.iterator();
		while (iter.hasNext()) {
			byte[] data = iter.next();

			System.out.println(ByteArrayUtil.toHexString(data));
		}
	}
}
