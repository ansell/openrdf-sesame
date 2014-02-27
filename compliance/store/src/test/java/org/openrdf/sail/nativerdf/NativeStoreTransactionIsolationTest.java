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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.aduna.io.FileUtil;

import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;

public class NativeStoreTransactionIsolationTest {

	private SailRepository m_repository;

	private File dataDir;

	@Before
	public void setUp()
		throws Exception
	{
		dataDir = FileUtil.createTempDir("nativestore");
		m_repository = new SailRepository(new NativeStore(dataDir));
		m_repository.initialize();
	}

	@After
	public void tearDown()
		throws Exception
	{
		try {
			m_repository.shutDown();
		}
		finally {
			FileUtil.deleteDir(dataDir);
		}
	}

	@Test
	public void testTransactionIsolationForRead()
		throws Exception
	{
		SailRepositoryConnection connection1 = m_repository.getConnection();
		try {
			connection1.begin();
			try {
				// Add but do not commit
				connection1.add(OWL.CLASS, RDFS.COMMENT, RDF.STATEMENT);
				assertTrue("Should be able to see uncommitted statement on same connection",
						connection1.hasStatement(OWL.CLASS, RDFS.COMMENT, RDF.STATEMENT, true));

				SailRepositoryConnection connection2 = m_repository.getConnection();
				try {
					assertFalse(
							"Should not be able to see uncommitted statement on separate connection outside transaction",
							connection2.hasStatement(OWL.CLASS, RDFS.COMMENT, RDF.STATEMENT, true));

					connection2.begin();
					try {
						assertFalse(
								"Should not be able to see uncommitted statement on separate connection inside transaction",
								connection2.hasStatement(OWL.CLASS, RDFS.COMMENT, RDF.STATEMENT, true));
					}
					finally {
						connection2.rollback();
					}
				}
				finally {
					connection2.close();
				}

			}
			finally {
				connection1.rollback();
			}

		}
		finally {
			connection1.close();
		}
	}

}
