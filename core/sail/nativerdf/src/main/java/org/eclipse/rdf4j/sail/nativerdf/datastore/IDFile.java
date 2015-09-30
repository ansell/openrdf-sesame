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
package org.eclipse.rdf4j.sail.nativerdf.datastore;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.eclipse.rdf4j.common.io.NioFile;

/**
 * Class supplying access to an ID file. An ID file maps IDs (integers &gt;= 1)
 * to file pointers (long integers). There is a direct correlation between IDs
 * and the position at which the file pointers are stored; the file pointer for
 * ID X is stored at position 8*X.
 * 
 * @author Arjohn Kampman
 */
public class IDFile {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * Magic number "Native ID File" to detect whether the file is actually an ID
	 * file. The first three bytes of the file should be equal to this magic
	 * number.
	 */
	private static final byte[] MAGIC_NUMBER = new byte[] { 'n', 'i', 'f' };

	/**
	 * File format version, stored as the fourth byte in ID files.
	 */
	private static final byte FILE_FORMAT_VERSION = 1;

	/**
	 * The size of the file header in bytes. The file header contains the
	 * following data: magic number (3 bytes) file format version (1 byte) and 4
	 * dummy bytes to align data at 8-byte offsets.
	 */
	private static final long HEADER_LENGTH = 8;

	private static final long ITEM_SIZE = 8L;

	/*-----------*
	 * Variables *
	 *-----------*/

	private final NioFile nioFile;

	private final boolean forceSync;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public IDFile(File file)
		throws IOException
	{
		this(file, false);
	}

	public IDFile(File file, boolean forceSync)
		throws IOException
	{
		this.nioFile = new NioFile(file);
		this.forceSync = forceSync;

		try {
			if (nioFile.size() == 0L) {
				// Empty file, write header
				nioFile.writeBytes(MAGIC_NUMBER, 0);
				nioFile.writeByte(FILE_FORMAT_VERSION, 3);
				nioFile.writeBytes(new byte[] { 0, 0, 0, 0 }, 4);

				sync();
			}
			else if (nioFile.size() < HEADER_LENGTH) {
				throw new IOException("File too small to be a compatible ID file");
			}
			else {
				// Verify file header
				if (!Arrays.equals(MAGIC_NUMBER, nioFile.readBytes(0, MAGIC_NUMBER.length))) {
					throw new IOException("File doesn't contain compatible ID records");
				}

				byte version = nioFile.readByte(MAGIC_NUMBER.length);
				if (version > FILE_FORMAT_VERSION) {
					throw new IOException("Unable to read ID file; it uses a newer file format");
				}
				else if (version != FILE_FORMAT_VERSION) {
					throw new IOException("Unable to read ID file; invalid file format version: " + version);
				}
			}
		}
		catch (IOException e) {
			this.nioFile.close();
			throw e;
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	public final File getFile() {
		return nioFile.getFile();
	}

	/**
	 * Gets the largest ID that is stored in this ID file.
	 * 
	 * @return The largest ID, or <tt>0</tt> if the file does not contain any
	 *         data.
	 * @throws IOException
	 *         If an I/O error occurs.
	 */
	public int getMaxID()
		throws IOException
	{
		return (int)(nioFile.size() / ITEM_SIZE) - 1;
	}

	/**
	 * Stores the offset of a new data entry, returning the ID under which is
	 * stored.
	 */
	public int storeOffset(long offset)
		throws IOException
	{
		long fileSize = nioFile.size();
		nioFile.writeLong(offset, fileSize);
		return (int)(fileSize / ITEM_SIZE);
	}

	/**
	 * Sets or updates the stored offset for the specified ID.
	 * 
	 * @param id
	 *        The ID to set the offset for, must be larger than 0.
	 * @param offset
	 *        The (new) offset for the specified ID.
	 */
	public void setOffset(int id, long offset)
		throws IOException
	{
		assert id > 0 : "id must be larger than 0, is: " + id;
		nioFile.writeLong(offset, ITEM_SIZE * id);
	}

	/**
	 * Gets the offset of the data entry with the specified ID.
	 * 
	 * @param id
	 *        The ID to get the offset for, must be larger than 0.
	 * @return The offset for the ID.
	 */
	public long getOffset(int id)
		throws IOException
	{
		assert id > 0 : "id must be larger than 0, is: " + id;
		return nioFile.readLong(ITEM_SIZE * id);
	}

	/**
	 * Discards all stored data.
	 * 
	 * @throws IOException
	 *         If an I/O error occurred.
	 */
	public void clear()
		throws IOException
	{
		nioFile.truncate(HEADER_LENGTH);
	}

	/**
	 * Syncs any unstored data to the hash file.
	 */
	public void sync()
		throws IOException
	{
		if (forceSync) {
			nioFile.force(false);
		}
	}

	/**
	 * Closes the ID file, releasing any file locks that it might have.
	 * 
	 * @throws IOException
	 */
	public void close()
		throws IOException
	{
		nioFile.close();
	}
}
