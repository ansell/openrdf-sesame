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
package org.openrdf.sail.nativerdf;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import info.aduna.io.NioFile;

/**
 * Writes transaction statuses to a file.
 */
class TxnStatusFile {

	public static enum TxnStatus {

		/**
		 * No active transaction. This occurs if no transaction has been started
		 * yet, or if all transactions have been committed or rolled back.
		 */
		NONE,

		/**
		 * A transaction has been started, but was not yet committed or rolled
		 * back.
		 */
		ACTIVE,

		/**
		 * A transaction is being committed.
		 */
		COMMITTING,

		/**
		 * A transaction is being rolled back.
		 */
		ROLLING_BACK,

		/**
		 * The transaction status is unknown.
		 */
		UNKNOWN;
	}

	private static final Charset US_ASCII = Charset.forName("US-ASCII");

	/**
	 * The name of the transaction status file.
	 */
	public static final String FILE_NAME = "txn-status";

	private final NioFile nioFile;

	/**
	 * Creates a new transaction status file. New files are initialized with
	 * {@link TxnStatus#NONE}.
	 * 
	 * @param dataDir
	 *        The directory for the transaction status file.
	 * @throws IOException
	 *         If the file did not yet exist and could not be written to.
	 */
	public TxnStatusFile(File dataDir)
		throws IOException
	{
		File statusFile = new File(dataDir, FILE_NAME);
		nioFile = new NioFile(statusFile, "rwd");

		if (nioFile.size() == 0) {
			setTxnStatus(TxnStatus.NONE);
		}
	}

	public void close()
		throws IOException
	{
		nioFile.close();
	}

	/**
	 * Writes the specified transaction status to file.
	 * 
	 * @param txnStatus
	 *        The transaction status to write.
	 * @throws IOException
	 *         If the transaction status could not be written to file.
	 */
	public void setTxnStatus(TxnStatus txnStatus)
		throws IOException
	{
		byte[] bytes = txnStatus.name().getBytes(US_ASCII);
		nioFile.truncate(bytes.length);
		nioFile.writeBytes(bytes, 0);
	}

	/**
	 * Reads the transaction status from file.
	 * 
	 * @return The read transaction status, or {@link TxnStatus#UNKNOWN} when the
	 *         file contains an unrecognized status string.
	 * @throws IOException
	 *         If the transaction status file could not be read.
	 */
	public TxnStatus getTxnStatus()
		throws IOException
	{
		byte[] bytes = nioFile.readBytes(0, (int)nioFile.size());
		String s = new String(bytes, US_ASCII);
		try {
			return TxnStatus.valueOf(s);
		}
		catch (IllegalArgumentException e) {
			// use platform encoding for backwards compatibility with versions
			// older than 2.6.6:
			s = new String(bytes);
			try {
				return TxnStatus.valueOf(s);
			}
			catch (IllegalArgumentException e2) {
				return TxnStatus.UNKNOWN;
			}
		}
	}
}
