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
package info.aduna.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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

	private volatile Path file;

	private final OpenOption[] mode;

	private volatile FileChannel raf;

	private volatile boolean explictlyClosed;

	public NioFile(Path file)
		throws IOException
	{
		this(file, StandardOpenOption.READ, StandardOpenOption.WRITE);
	}

	public NioFile(Path file, OpenOption... mode)
		throws IOException
	{
		this.file = file;
		this.mode = mode;

		if (!Files.exists(file)) {
			this.file = Files.createFile(file);
		}

		explictlyClosed = false;
		open();
	}

	private void open()
		throws IOException
	{
		raf = FileChannel.open(file, mode);
		// file.newByteChannel();
		// raf = AsynchronousFileChannel.open(file, mode);
	}

	private synchronized void reopen(ClosedChannelException e)
		throws IOException
	{
		if (explictlyClosed) {
			throw e;
		}
		if (raf.isOpen()) {
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

	public Path getPath() {
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
	public void delete()
		throws IOException
	{
		close();
		Files.delete(file);
	}

	/**
	 * Performs a protected {@link FileChannel#force(boolean)} call.
	 */
	public void force(boolean metaData)
		throws IOException
	{
		while (true) {
			try {
				raf.force(metaData);
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
				raf.truncate(size);
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
				return raf.size();
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
				return raf.transferTo(position, count, target);
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
		return raf.write(buf, offset);
	}

	/**
	 * Performs a protected {@link FileChannel#read(ByteBuffer, long)} call.
	 */
	public int read(ByteBuffer buf, long offset)
		throws IOException
	{
		while (true) {
			try {
				return raf.read(buf, offset);
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
