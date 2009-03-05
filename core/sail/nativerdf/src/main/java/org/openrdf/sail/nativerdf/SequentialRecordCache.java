/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
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
class SequentialRecordCache extends RecordCache {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected final File cacheFile;

	protected final RandomAccessFile raf;

	protected final FileChannel fileChannel;

	protected final int recordSize;

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
		raf = new RandomAccessFile(cacheFile, "rw");
		fileChannel = raf.getChannel();
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
	public void storeRecordInternal(byte[] data)
		throws IOException
	{
		fileChannel.write(ByteBuffer.wrap(data), fileChannel.size());
	}

	@Override
	public RecordIterator getRecordsInternal() {
		return new RecordCacheIterator();
	}

	/*---------------------------------*
	 * Inner class RecordCacheIterator *
	 *---------------------------------*/

	protected class RecordCacheIterator implements RecordIterator {

		private long position = 0L;

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
			if (position >= recordSize && position <= fileChannel.size()) {
				fileChannel.write(ByteBuffer.wrap(value), position - recordSize);
			}
		}

		public void close()
			throws IOException
		{
		}
	}
}
