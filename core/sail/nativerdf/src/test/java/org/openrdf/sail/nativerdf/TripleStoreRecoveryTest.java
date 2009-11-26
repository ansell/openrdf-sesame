/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;

import junit.framework.TestCase;

import info.aduna.io.FileUtil;

import org.openrdf.sail.nativerdf.TxnStatusFile.TxnStatus;
import org.openrdf.sail.nativerdf.btree.RecordIterator;

/**
 * An extension of RDFStoreTest for testing the class {@link NativeStore}.
 */
public class TripleStoreRecoveryTest extends TestCase {

	private File dataDir;

	public TripleStoreRecoveryTest(String name) {
		super(name);
	}

	@Override
	protected void setUp()
		throws Exception
	{
		super.setUp();
		dataDir = FileUtil.createTempDir("nativestore");
	}

	@Override
	protected void tearDown()
		throws Exception
	{
		FileUtil.deleteDir(dataDir);
		dataDir = null;

		super.tearDown();
	}

	public void testRollbackRecovery()
		throws Exception
	{
		TripleStore tripleStore = new TripleStore(dataDir, "spoc");
		try {
			tripleStore.startTransaction();
			tripleStore.storeTriple(1, 2, 3, 4);
			// forget to commit or tollback
		}
		finally {
			tripleStore.close();
		}

		// Try to restore from the uncompleted transaction
		tripleStore = new TripleStore(dataDir, "spoc");
		try {
			RecordIterator iter = tripleStore.getTriples(-1, -1, -1, -1);
			try {
				assertNull(iter.next());
			}
			finally {
				iter.close();
			}
		}
		finally {
			tripleStore.close();
		}
	}

	public void testCommitRecovery()
		throws Exception
	{
		TripleStore tripleStore = new TripleStore(dataDir, "spoc");
		try {
			tripleStore.startTransaction();
			tripleStore.storeTriple(1, 2, 3, 4);
			// forget to commit or tollback
		}
		finally {
			tripleStore.close();
		}

		// Pretend that commit was called
		TxnStatusFile txnStatusFile = new TxnStatusFile(dataDir);
		txnStatusFile.setTxnStatus(TxnStatus.COMMITTING);

		// Try to restore from the uncompleted transaction
		tripleStore = new TripleStore(dataDir, "spoc");
		try {
			RecordIterator iter = tripleStore.getTriples(-1, -1, -1, -1);
			try {
				// iter should contain exactly one element
				assertNotNull(iter.next());
				assertNull(iter.next());
			}
			finally {
				iter.close();
			}
		}
		finally {
			tripleStore.close();
		}
	}

}
