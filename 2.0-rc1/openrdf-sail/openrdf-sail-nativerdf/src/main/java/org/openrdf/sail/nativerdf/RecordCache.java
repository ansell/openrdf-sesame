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

import org.openrdf.sail.nativerdf.btree.BTreeIterator;

/**
 * A cache for fixed size byte array records. This cache uses a temporary file
 * to store the records. This file is deleted upon calling {@link #discard()}.
 * 
 * @author Arjohn Kampman
 */
class RecordCache {

	private final File cacheFile;

	private final RandomAccessFile raf;

	private final FileChannel fileChannel;

	private long fileSize;

	private final int recordSize;

	private final long maxFileSize;

	public RecordCache(File cacheDir, int recordSize)
		throws IOException
	{
		this(cacheDir, recordSize, Long.MAX_VALUE - recordSize);
	}

	public RecordCache(File cacheDir, int recordSize, long maxFileSize)
		throws IOException
	{
		this.cacheFile = File.createTempFile("records", ".tmp", cacheDir);
		this.recordSize = recordSize;
		this.maxFileSize = maxFileSize;

		raf = new RandomAccessFile(cacheFile, "rw");
		fileChannel = raf.getChannel();
		fileSize = fileChannel.size();
	}

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

	public final boolean isValid() {
		return fileSize < maxFileSize;
	}

	public void storeRecord(byte[] data)
		throws IOException
	{
		if (isValid()) {
			fileChannel.write(ByteBuffer.wrap(data), fileSize);
			fileSize += data.length;
		}
	}

	public void append(RecordCache other)
		throws IOException
	{
		if (isValid()) {
			fileChannel.position(fileChannel.size());
			other.fileChannel.transferTo(0L, other.fileChannel.size(), fileChannel);
			fileSize = fileChannel.size();
		}
	}

	public BTreeIterator getRecords() {
		if (isValid()) {
			return new RecordCacheIteration();
		}

		throw new IllegalStateException();
	}

	public long getRecordCount() {
		if (isValid()) {
			return fileSize / recordSize;
		}

		throw new IllegalStateException();
	}

	/*----------------------------------*
	 * Inner class RecordCacheIteration *
	 *----------------------------------*/

	public class RecordCacheIteration implements BTreeIterator {

		private long position = 0L;

		public byte[] next()
			throws IOException
		{
			if (position + recordSize <= fileSize) {
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
			if (position >= recordSize && position <= fileSize) {
				fileChannel.write(ByteBuffer.wrap(value), position - recordSize);
			}
		}

		public void close()
			throws IOException
		{
		}
	}
}
