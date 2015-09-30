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
package org.eclipse.rdf4j.sail.nativerdf;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

import org.eclipse.rdf4j.common.io.NioFile;
import org.eclipse.rdf4j.sail.nativerdf.btree.RecordIterator;

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

	private final NioFile nioFile;

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

		File cacheFile = File.createTempFile("txncache", ".dat", cacheDir);
		nioFile = new NioFile(cacheFile);

		// Write file header
		nioFile.writeBytes(MAGIC_NUMBER, 0);
		nioFile.writeByte(FILE_FORMAT_VERSION, MAGIC_NUMBER.length);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void discard()
		throws IOException
	{
		nioFile.delete();
	}

	@Override
	protected void clearInternal()
		throws IOException
	{
		nioFile.truncate(HEADER_LENGTH);
	}

	@Override
	protected void storeRecordInternal(byte[] data)
		throws IOException
	{
		nioFile.writeBytes(data, nioFile.size());
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
			if (position + recordSize <= nioFile.size()) {
				byte[] data = new byte[recordSize];
				ByteBuffer buf = ByteBuffer.wrap(data);

				int bytesRead = nioFile.read(buf, position);

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
			if (position >= HEADER_LENGTH + recordSize && position <= nioFile.size()) {
				nioFile.writeBytes(value, position - recordSize);
			}
		}

		public void close()
			throws IOException
		{
		}
	}
}
