/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;
import java.io.IOException;

import info.aduna.io.IOUtil;

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
		 * The transaction status is unkown.
		 */
		UNKNOWN;
	}

	/**
	 * The name of the transaction status file.
	 */
	public static final String FILE_NAME = "txn-status";

	private final File file;

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
		this.file = new File(dataDir, FILE_NAME);

		if (!file.exists()) {
			setTxnStatus(TxnStatus.NONE);
		}
	}

	/**
	 * Writes the specfied transaction status to file.
	 * 
	 * @param txnStatus
	 *        The transaction status to write.
	 * @throws IOException
	 *         If the transaction status could not be written to file.
	 */
	public void setTxnStatus(TxnStatus txnStatus)
		throws IOException
	{
		IOUtil.writeString(txnStatus.name(), file);
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
		String s = IOUtil.readString(file);
		try {
			return TxnStatus.valueOf(s);
		}
		catch (IllegalArgumentException e) {
			return TxnStatus.UNKNOWN;
		}
	}
}
