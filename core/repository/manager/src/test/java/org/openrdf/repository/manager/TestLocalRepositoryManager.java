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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.aduna.io.FileUtil;

import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.memory.config.MemoryStoreConfig;

/**
 * @author jeen
 */
public class TestLocalRepositoryManager {

	private LocalRepositoryManager manager;

	private File datadir;

	private String testRep = "test";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		datadir = FileUtil.createTempDir("local-repositorymanager-test");
		manager = new LocalRepositoryManager(datadir);
		manager.initialize();

		// create a configuration for the SAIL stack
		SailImplConfig backendConfig = new MemoryStoreConfig();

		// create a configuration for the repository implementation
		RepositoryImplConfig repositoryTypeSpec = new SailRepositoryConfig(backendConfig);
		RepositoryConfig config = new RepositoryConfig(testRep, repositoryTypeSpec);

		manager.addRepositoryConfig(config);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown()
		throws Exception
	{
		manager.shutDown();
		FileUtil.deleteDir(datadir);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.repository.manager.LocalRepositoryManager#getRepository(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetRepository() throws Exception {

		Repository rep = manager.getRepository(testRep);
		assertNotNull(rep);
		assertTrue(rep.isInitialized());
		
		rep.shutDown();
		
		rep = manager.getRepository(testRep);
		assertNotNull(rep);
		assertTrue(rep.isInitialized());
		
	}

}
