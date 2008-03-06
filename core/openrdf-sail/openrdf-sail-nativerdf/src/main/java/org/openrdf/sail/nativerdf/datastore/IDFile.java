/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.datastore;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * Class supplying access to an ID file. An ID file maps IDs (integers &gt;= 1)
 * to file pointers (long integers). There is a direct correlation between IDs
 * and the position at which the file pointers are stored; the file pointer for
 * ID X is stored at position 8*X.
 */
public class IDFile {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * Magic number "Native ID File" to detect whether the file is actually an
	 * ID file. The first three bytes of the file should be equal to this
	 * magic number.
	 */
	private static final byte[] MAGIC_NUMBER = new byte[] {'n', 'i', 'f'};
	
	/**
	 * File format version, stored as the fourth byte in ID files.
	 */
	private static final byte FILE_FORMAT_VERSION = 1;

	/**
	 * The size of the file header in bytes. The file header contains the
	 * following data: magic number (3 bytes) file format version (1 byte) and
	 * 4 dummy bytes to align data at 8-byte offsets.
	 */
	private static final long HEADER_LENGTH = 8;
	
	private static final long ITEM_SIZE = 8L;

	/*-----------*
	 * Variables *
	 *-----------*/

	private File _file;
	private RandomAccessFile _raf;
	private FileChannel _fileChannel;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public IDFile(File file)
		throws IOException
	{
		_file = file;
		
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
			// Empty file, write header
			ByteBuffer buf = ByteBuffer.allocate((int)HEADER_LENGTH);
			buf.put(MAGIC_NUMBER);
			buf.put(FILE_FORMAT_VERSION);
			buf.put(new byte[] {0, 0, 0, 0});
			buf.rewind();
			
			_fileChannel.write(buf, 0L);
		}
		else {
			// Verify file header
			ByteBuffer buf = ByteBuffer.allocate((int)HEADER_LENGTH);
			_fileChannel.read(buf, 0L);
			buf.rewind();
			
			if (buf.remaining() < HEADER_LENGTH) {
				throw new IOException("File too short to be a compatible ID file");
			}

			byte[] magicNumber = new byte[MAGIC_NUMBER.length];
			buf.get(magicNumber);
			byte version = buf.get();
			
			if (!Arrays.equals(MAGIC_NUMBER, magicNumber)) {
				throw new IOException("File doesn't contain compatible ID records");
			}
			
			if (version > FILE_FORMAT_VERSION) {
				throw new IOException("Unable to read ID file; it uses a newer file format");
			}
			else if (version != FILE_FORMAT_VERSION) {
				throw new IOException("Unable to read ID file; invalid file format version: " + version);
			}
		}

	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the largest ID that is stored in this ID file.
	 * 
	 * @return The largest ID, or <tt>0</tt> if the file does not contain any
	 * data.
	 * @throws IOException If an I/O error occurs.
	 */
	public int getMaxID()
		throws IOException
	{
		return (int)(_fileChannel.size() / ITEM_SIZE) - 1;
	}

	/**
	 * Stores the offset of a new data entry, returning the ID under which is
	 * stored.
	 */
	public int storeOffset(long offset)
		throws IOException
	{
		int id = (int)(_fileChannel.size() / ITEM_SIZE);
		setOffset(id, offset);
		return id;
	}

	/**
	 * Sets or updates the stored offset for the specified ID.
	 * 
	 * @param id The ID to set the offset for, must be larger than 0.
	 * @param offset The (new) offset for the specified ID.
	 */
	public void setOffset(int id, long offset)
		throws IOException
	{
		assert id > 0 : "id must be larger than 0, is: " + id;

		ByteBuffer buf = ByteBuffer.allocate(8);
		buf.putLong(0, offset);
		_fileChannel.write(buf, ITEM_SIZE * id);
	}

	/**
	 * Gets the offset of the data entry with the specified ID.
	 * 
	 * @param id The ID to get the offset for, must be larger than 0.
	 * @return The offset for the ID.
	 */
	public long getOffset(int id)
		throws IOException
	{
		assert id > 0 : "id must be larger than 0, is: " + id;

		ByteBuffer buf = ByteBuffer.allocate(8);
		_fileChannel.read(buf, ITEM_SIZE * id);
		return buf.getLong(0);
	}

	/**
	 * Discards all stored data.
	 * 
	 * @throws IOException If an I/O error occurred.
	 */
	public void clear()
		throws IOException
	{
		_fileChannel.truncate(HEADER_LENGTH);
	}
	
	/**
	 * Syncs any unstored data to the hash file.
	 */
	public void sync()
		throws IOException
	{
		// no-op, for now
	}
	
	/**
	 * Closes the ID file, releasing any file locks that it might have.
	 *
	 * @throws IOException
	 */
	public void close()
		throws IOException
	{
		_raf.close();
	}
}
