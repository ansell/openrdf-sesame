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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.aduna.io.FileUtil;

import org.openrdf.sail.nativerdf.TxnStatusFile.TxnStatus;
import org.openrdf.sail.nativerdf.btree.RecordIterator;

/**
 * An extension of RDFStoreTest for testing the class {@link NativeStore}.
 */
public class TripleStoreRecoveryTest {

	private File dataDir;

	@Before
	public void setUp()
		throws Exception
	{
		dataDir = FileUtil.createTempDir("nativestore");
	}

	@After
	public void tearDown()
		throws Exception
	{
		FileUtil.deleteDir(dataDir);
		dataDir = null;
	}

	@Test
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

	@Test
	public void testCommitRecovery()
		throws Exception
	{
		TripleStore tripleStore = new TripleStore(dataDir, "spoc");
		try {
			tripleStore.startTransaction();
			tripleStore.storeTriple(1, 2, 3, 4);
			// forget to commit or rollback
		}
		finally {
			tripleStore.close();
		}

		// Pretend that commit was called
		TxnStatusFile txnStatusFile = new TxnStatusFile(dataDir);
		try {
			txnStatusFile.setTxnStatus(TxnStatus.COMMITTING);
		}
		finally {
			txnStatusFile.close();
		}

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
