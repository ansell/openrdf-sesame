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
package org.openrdf.repository.manager;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import info.aduna.io.FileUtil;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.sail.config.ProxyRepositoryConfig;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.sail.memory.config.MemoryStoreConfig;

/**
 * @author jeen
 */
public class TestLocalRepositoryManager {

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
				new MemoryStoreConfig())));

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
	 * {@link org.openrdf.repository.manager.LocalRepositoryManager#getRepository(java.lang.String)}
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
	public void testRestartManager() throws Exception
	{
		Repository rep = manager.getRepository(TEST_REPO);
		assertNotNull("Expected repository to exist.", rep);
		assertTrue("Expected repository to be initialized.", rep.isInitialized());
		rep.shutDown();
		manager.shutDown();
		
		manager = new LocalRepositoryManager(datadir);
		manager.initialize();
		Repository rep2 = manager.getRepository(TEST_REPO);
		assertNotNull("Expected repository to exist.", rep2);
		assertTrue("Expected repository to be initialized.", rep2.isInitialized());
		rep2.shutDown();
		
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
