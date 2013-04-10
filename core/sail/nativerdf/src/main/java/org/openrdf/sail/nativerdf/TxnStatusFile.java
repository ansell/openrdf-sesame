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
package org.openrdf.sail.nativerdf;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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
	public TxnStatusFile(Path dataDir)
		throws IOException
	{
		Path statusFile = dataDir.resolve(FILE_NAME);
		nioFile = new NioFile(statusFile, StandardOpenOption.READ, StandardOpenOption.WRITE);

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
