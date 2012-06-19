/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
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
