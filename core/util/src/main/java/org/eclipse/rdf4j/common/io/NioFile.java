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
package org.eclipse.rdf4j.common.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

/**
 * File wrapper that protects against concurrent file closing events due to e.g.
 * {@link Thread#interrupt() thread interrupts}. In case the file channel that
 * is used by this class is closed due to such an event, it will try to reopen
 * the channel. The thread that causes the {@link ClosedByInterruptException} is
 * not protected, assuming the interrupt is intended to end the thread's
 * operation.
 * 
 * @author Arjohn Kampman
 */
public final class NioFile {

	private final File file;

	private final String mode;

	private volatile RandomAccessFile raf;

	private volatile FileChannel fc;

	private volatile boolean explictlyClosed;

	public NioFile(File file)
		throws IOException
	{
		this(file, "rw");
	}

	public NioFile(File file, String mode)
		throws IOException
	{
		this.file = file;
		this.mode = mode;

		if (!file.exists()) {
			boolean created = file.createNewFile();
			if (!created) {
				throw new IOException("Failed to create file: " + file);
			}
		}

		explictlyClosed = false;
		open();
	}

	private void open()
		throws IOException
	{
		raf = new RandomAccessFile(file, mode);
		fc = raf.getChannel();
	}

	private synchronized void reopen(ClosedChannelException e)
		throws IOException
	{
		if (explictlyClosed) {
			throw e;
		}
		if (fc.isOpen()) {
			// file channel has been already reopened by another thread
			return;
		}
		open();
	}

	public synchronized void close()
		throws IOException
	{
		explictlyClosed = true;
		raf.close();
	}

	public boolean isClosed() {
		return explictlyClosed;
	}

	public File getFile() {
		return file;
	}

	/**
	 * Closed any open channels and then deletes the file.
	 * 
	 * @return <tt>true</tt> if the file has been deleted successfully,
	 *         <tt>false</tt> otherwise.
	 * @throws IOException
	 *         If there was a problem closing the open file channel.
	 */
	public boolean delete()
		throws IOException
	{
		close();
		return file.delete();
	}

	/**
	 * Performs a protected {@link FileChannel#force(boolean)} call.
	 */
	public void force(boolean metaData)
		throws IOException
	{
		while (true) {
			try {
				fc.force(metaData);
				return;
			}
			catch (ClosedByInterruptException e) {
				throw e;
			}
			catch (ClosedChannelException e) {
				reopen(e);
			}
		}
	}

	/**
	 * Performs a protected {@link FileChannel#truncate(long)} call.
	 */
	public void truncate(long size)
		throws IOException
	{
		while (true) {
			try {
				fc.truncate(size);
				return;
			}
			catch (ClosedByInterruptException e) {
				throw e;
			}
			catch (ClosedChannelException e) {
				reopen(e);
			}
		}
	}

	/**
	 * Performs a protected {@link FileChannel#size()} call.
	 */
	public long size()
		throws IOException
	{
		while (true) {
			try {
				return fc.size();
			}
			catch (ClosedByInterruptException e) {
				throw e;
			}
			catch (ClosedChannelException e) {
				reopen(e);
			}
		}
	}

	/**
	 * Performs a protected
	 * {@link FileChannel#transferTo(long, long, WritableByteChannel)} call.
	 */
	public long transferTo(long position, long count, WritableByteChannel target)
		throws IOException
	{
		while (true) {
			try {
				return fc.transferTo(position, count, target);
			}
			catch (ClosedByInterruptException e) {
				throw e;
			}
			catch (ClosedChannelException e) {
				reopen(e);
			}
		}
	}

	/**
	 * Performs a protected {@link FileChannel#write(ByteBuffer, long)} call.
	 */
	public int write(ByteBuffer buf, long offset)
		throws IOException
	{
		while (true) {
			try {
				return fc.write(buf, offset);
			}
			catch (ClosedByInterruptException e) {
				throw e;
			}
			catch (ClosedChannelException e) {
				reopen(e);
			}
		}
	}

	/**
	 * Performs a protected {@link FileChannel#read(ByteBuffer, long)} call.
	 */
	public int read(ByteBuffer buf, long offset)
		throws IOException
	{
		while (true) {
			try {
				return fc.read(buf, offset);
			}
			catch (ClosedByInterruptException e) {
				throw e;
			}
			catch (ClosedChannelException e) {
				reopen(e);
			}
		}
	}

	public void writeBytes(byte[] value, long offset)
		throws IOException
	{
		write(ByteBuffer.wrap(value), offset);
	}

	public byte[] readBytes(long offset, int length)
		throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(length);
		read(buf, offset);
		return buf.array();
	}

	public void writeByte(byte value, long offset)
		throws IOException
	{
		writeBytes(new byte[] { value }, offset);
	}

	public byte readByte(long offset)
		throws IOException
	{
		return readBytes(offset, 1)[0];
	}

	public void writeLong(long value, long offset)
		throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(8);
		buf.putLong(0, value);
		write(buf, offset);
	}

	public long readLong(long offset)
		throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(8);
		read(buf, offset);
		return buf.getLong(0);
	}

	public void writeInt(int value, long offset)
		throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt(0, value);
		write(buf, offset);
	}

	public int readInt(long offset)
		throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(4);
		read(buf, offset);
		return buf.getInt(0);
	}
}
