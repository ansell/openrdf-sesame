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
import java.util.NoSuchElementException;

/**
 * Class supplying access to a data file. A data file stores data sequentially.
 * Each entry starts with the entry's length (4 bytes), followed by the data
 * itself. File offsets are used to identify entries.
 */
public class DataFile {
	
	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * Magic number "Native Data File" to detect whether the file is actually a
	 * data file. The first three bytes of the file should be equal to this
	 * magic number.
	 */
	private static final byte[] MAGIC_NUMBER = new byte[] {'n', 'd', 'f'};
	
	/**
	 * File format version, stored as the fourth byte in data files.
	 */
	private static final byte FILE_FORMAT_VERSION = 1;

	private static final long HEADER_LENGTH = MAGIC_NUMBER.length + 1;

	/*-----------*
	 * Variables *
	 *-----------*/

	private File _file;
	private RandomAccessFile _raf;
	private FileChannel _fileChannel;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public DataFile(File file)
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
		_raf = new RandomAccessFile(file, "rw");
		_fileChannel = _raf.getChannel();

		if (_fileChannel.size() == 0L) {
			// Empty file, write header
			ByteBuffer buf = ByteBuffer.allocate((int)HEADER_LENGTH);
			buf.put(MAGIC_NUMBER);
			buf.put(FILE_FORMAT_VERSION);
			buf.rewind();
			
			_fileChannel.write(buf, 0L);
		}
		else {
			// Verify file header
			ByteBuffer buf = ByteBuffer.allocate((int)HEADER_LENGTH);
			_fileChannel.read(buf, 0L);
			buf.rewind();
			
			if (buf.remaining() < HEADER_LENGTH) {
				throw new IOException("File too short to be a compatible data file");
			}

			byte[] magicNumber = new byte[MAGIC_NUMBER.length];
			buf.get(magicNumber);
			byte version = buf.get();
			
			if (!Arrays.equals(MAGIC_NUMBER, magicNumber)) {
				throw new IOException("File doesn't contain compatible data records");
			}
			
			if (version > FILE_FORMAT_VERSION) {
				throw new IOException("Unable to read data file; it uses a newer file format");
			}
			else if (version != FILE_FORMAT_VERSION) {
				throw new IOException("Unable to read data file; invalid file format version: " + version);
			}
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Stores the specified data and returns the byte-offset at which it has
	 * been stored.
	 * 
	 * @param data The data to store, must not be <tt>null</tt>.
	 * @return The byte-offset in the file at which the data was stored.
	 */
	public long storeData(byte[] data)
		throws IOException
	{
		assert data != null : "data must not be null";
		
		long offset = _fileChannel.size();

		ByteBuffer buf = ByteBuffer.allocate(data.length + 4);
		buf.putInt(data.length);
		buf.put(data);
		buf.rewind();

		_fileChannel.write(buf, offset);

		return offset;
	}

	/**
	 * Gets the data that is stored at the specified offset.
	 *
	 * @param offset An offset in the data file, must be larger than 0.
	 * @return The data that was found on the specified offset.
	 * @exception IOException If an I/O error occurred.
	 */
	public byte[] getData(long offset)
		throws IOException
	{
		assert offset > 0 : "offset must be larger than 0, is: " + offset;

		// TODO: maybe get more data in one go is more efficient?
		ByteBuffer buf = ByteBuffer.allocate(4);
		_fileChannel.read(buf, offset);
		int dataLength = buf.getInt(0);

		byte[] data = new byte[dataLength];
		buf = ByteBuffer.wrap(data);
		_fileChannel.read(buf, offset + 4L);

		return data;
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
	 * Closes the data file, releasing any file locks that it might have.
	 *
	 * @throws IOException
	 */
	public void close()
		throws IOException
	{
		_raf.close();
	}

	/**
	 * Gets an iterator that can be used to iterate over all stored data.
	 * 
	 * @return a DataIterator.
	 */
	public DataIterator iterator() {
		return new DataIterator();
	}

	/**
	 * An iterator that iterates over the data that is stored in a data file.
	 */
	public class DataIterator {

		private long _position = HEADER_LENGTH;

		public boolean hasNext()
			throws IOException
		{
			return _position < _fileChannel.size();
		}

		public byte[] next()
			throws IOException
		{
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			byte[] data = getData(_position);
			_position += (4 + data.length);
			return data;
		}
	}
}
