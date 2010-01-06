/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.NoSuchElementException;

import org.openrdf.sail.nativerdf.btree.RecordIterator;

/**
 * A cache for fixed size byte array records. This cache uses a temporary file
 * to store the records. This file is deleted upon calling {@link #discard()}.
 * 
 * @author Arjohn Kampman
 */
final class SequentialRecordCache extends RecordCache {

	/**
	 * Magic number "Sequential Record Cache" to detect whether the file is
	 * actually a sequential record cache file. The first three bytes of the file
	 * should be equal to this magic number.
	 */
	private static final byte[] MAGIC_NUMBER = new byte[] { 's', 'r', 'c' };

	/**
	 * The file format version number, stored as the fourth byte in sequential
	 * record cache files.
	 */
	private static final byte FILE_FORMAT_VERSION = 1;

	private static final int HEADER_LENGTH = MAGIC_NUMBER.length + 1;

	/*------------*
	 * Attributes *
	 *------------*/

	private final File cacheFile;

	private final RandomAccessFile raf;

	private final FileChannel fileChannel;

	private final int recordSize;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SequentialRecordCache(File cacheDir, int recordSize)
		throws IOException
	{
		this(cacheDir, recordSize, Long.MAX_VALUE);
	}

	public SequentialRecordCache(File cacheDir, int recordSize, long maxRecords)
		throws IOException
	{
		super(maxRecords);
		this.recordSize = recordSize;

		this.cacheFile = File.createTempFile("txncache", ".dat", cacheDir);
		this.raf = new RandomAccessFile(cacheFile, "rw");
		this.fileChannel = raf.getChannel();
		
		// Write file header
		raf.write(MAGIC_NUMBER);
		raf.write(FILE_FORMAT_VERSION);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void discard()
		throws IOException
	{
		try {
			try {
				fileChannel.close();
			}
			finally {
				raf.close();
			}
		}
		finally {
			cacheFile.delete();
		}
	}

	@Override
	protected void clearInternal()
		throws IOException
	{
		fileChannel.truncate(HEADER_LENGTH);
	}

	@Override
	protected void storeRecordInternal(byte[] data)
		throws IOException
	{
		fileChannel.write(ByteBuffer.wrap(data), fileChannel.size());
	}

	@Override
	protected RecordIterator getRecordsInternal() {
		return new RecordCacheIterator();
	}

	/*---------------------------------*
	 * Inner class RecordCacheIterator *
	 *---------------------------------*/

	protected class RecordCacheIterator implements RecordIterator {

		private long position = HEADER_LENGTH;

		public byte[] next()
			throws IOException
		{
			if (position + recordSize <= fileChannel.size()) {
				byte[] data = new byte[recordSize];
				ByteBuffer buf = ByteBuffer.wrap(data);

				int bytesRead = fileChannel.read(buf, position);

				if (bytesRead < 0) {
					throw new NoSuchElementException("No more elements available");
				}

				position += bytesRead;

				return data;
			}

			return null;
		}

		public void set(byte[] value)
			throws IOException
		{
			if (position >= HEADER_LENGTH + recordSize && position <= fileChannel.size()) {
				fileChannel.write(ByteBuffer.wrap(value), position - recordSize);
			}
		}

		public void close()
			throws IOException
		{
		}
	}
}
