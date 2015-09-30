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
package org.eclipse.rdf4j.repository.manager;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.sail.config.ProxyRepositoryConfig;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.sail.memory.config.MemoryStoreConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author jeen
 */
public class LocalRepositoryManagerTest {

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	private LocalRepositoryManager manager;

	private File datadir;

	private static final String TEST_REPO = "test";

	private static final String PROXY_ID = "proxy";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		datadir = tempDir.newFolder("local-repositorymanager-test");
		manager = new LocalRepositoryManager(datadir);
		manager.initialize();

		// Create configurations for the SAIL stack, and the repository
		// implementation.
		manager.addRepositoryConfig(new RepositoryConfig(TEST_REPO, new SailRepositoryConfig(
				new MemoryStoreConfig(true))));

		// Create configuration for proxy repository to previous repository.
		manager.addRepositoryConfig(new RepositoryConfig(PROXY_ID, new ProxyRepositoryConfig(TEST_REPO)));
	}

	/**
	 * @throws IOException
	 *         if a problem occurs deleting temporary resources
	 */
	@After
	public void tearDown()
		throws IOException
	{
		manager.shutDown();
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.repository.manager.LocalRepositoryManager#getRepository(java.lang.String)}
	 * .
	 * 
	 * @throws RepositoryException
	 *         if a problem occurs accessing the repository
	 * @throws RepositoryConfigException
	 *         if a problem occurs accessing the repository
	 */
	@Test
	public void testGetRepository()
		throws RepositoryConfigException, RepositoryException
	{
		Repository rep = manager.getRepository(TEST_REPO);
		assertNotNull("Expected repository to exist.", rep);
		assertTrue("Expected repository to be initialized.", rep.isInitialized());
		rep.shutDown();
		rep = manager.getRepository(TEST_REPO);
		assertNotNull("Expected repository to exist.", rep);
		assertTrue("Expected repository to be initialized.", rep.isInitialized());
	}

	@Test
	public void testRestartManagerWithoutTransaction()
		throws Exception
	{
		Repository rep = manager.getRepository(TEST_REPO);
		assertNotNull("Expected repository to exist.", rep);
		assertTrue("Expected repository to be initialized.", rep.isInitialized());
		RepositoryConnection conn = rep.getConnection();
		try {
			conn.add(conn.getValueFactory().createIRI("urn:sesame:test:subject"), RDF.TYPE, OWL.ONTOLOGY);
			assertEquals(1, conn.size());
		}
		finally {
			conn.close();
			rep.shutDown();
			manager.shutDown();
		}

		manager = new LocalRepositoryManager(datadir);
		manager.initialize();
		Repository rep2 = manager.getRepository(TEST_REPO);
		assertNotNull("Expected repository to exist.", rep2);
		assertTrue("Expected repository to be initialized.", rep2.isInitialized());
		RepositoryConnection conn2 = rep2.getConnection();
		try {
			assertEquals(1, conn2.size());
		}
		finally {
			conn2.close();
			rep2.shutDown();
			manager.shutDown();
		}

	}

	@Test
	public void testRestartManagerWithTransaction()
		throws Exception
	{
		Repository rep = manager.getRepository(TEST_REPO);
		assertNotNull("Expected repository to exist.", rep);
		assertTrue("Expected repository to be initialized.", rep.isInitialized());
		RepositoryConnection conn = rep.getConnection();
		try {
			conn.begin();
			conn.add(conn.getValueFactory().createIRI("urn:sesame:test:subject"), RDF.TYPE, OWL.ONTOLOGY);
			conn.commit();
			assertEquals(1, conn.size());
		}
		finally {
			conn.close();
			rep.shutDown();
			manager.shutDown();
		}

		manager = new LocalRepositoryManager(datadir);
		manager.initialize();
		Repository rep2 = manager.getRepository(TEST_REPO);
		assertNotNull("Expected repository to exist.", rep2);
		assertTrue("Expected repository to be initialized.", rep2.isInitialized());
		RepositoryConnection conn2 = rep2.getConnection();
		try {
			assertEquals(1, conn2.size());
		}
		finally {
			conn2.close();
			rep2.shutDown();
			manager.shutDown();
		}

	}

	/**
	 * Test method for {@link RepositoryManager.isSafeToRemove(String)}.
	 * 
	 * @throws RepositoryException
	 *         if a problem occurs during execution
	 * @throws RepositoryConfigException
	 *         if a problem occurs during execution
	 */
	@Test
	public void testIsSafeToRemove()
		throws RepositoryException, RepositoryConfigException
	{
		assertThat(manager.isSafeToRemove(PROXY_ID), is(equalTo(true)));
		assertThat(manager.isSafeToRemove(TEST_REPO), is(equalTo(false)));
		manager.removeRepository(PROXY_ID);
		assertThat(manager.hasRepositoryConfig(PROXY_ID), is(equalTo(false)));
		assertThat(manager.isSafeToRemove(TEST_REPO), is(equalTo(true)));
	}
}
