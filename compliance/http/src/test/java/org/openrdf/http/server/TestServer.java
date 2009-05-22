/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import java.io.File;
import java.io.IOException;

import info.aduna.io.FileUtil;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.sail.inferencer.fc.config.ForwardChainingRDFSInferencerConfig;
import org.openrdf.sail.memory.config.MemoryStoreConfig;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

/**
 * @author Herko ter Horst
 * @author James Leigh
 */
public class TestServer {

	public static final String TEST_REPO_ID = "Test";

	public static final String TEST_INFERENCE_REPO_ID = "Test-RDFS";

	public static int DEFAULT_PORT = 18080;

	public static String SERVER_URL = "http://localhost:" + DEFAULT_PORT;

	public static String REPOSITORY_URL = Protocol.getRepositoryLocation(TestServer.SERVER_URL, TEST_REPO_ID);

	public static String INFERENCE_REPOSITORY_URL = Protocol.getRepositoryLocation(TestServer.SERVER_URL,
			TEST_INFERENCE_REPO_ID);

	private SesameServer server;
	
	private final File dataDir;

	public TestServer()
		throws StoreConfigException, IOException
	{
		dataDir = FileUtil.createTempDir("sesame-test");
		server = new SesameServer(dataDir, DEFAULT_PORT);
	}

	public void start()
		throws Exception
	{
		server.start();
		createTestRepositories();
	}

	public void stop()
		throws Exception
	{
		server.stop();

		try {
			FileUtil.deleteDir(dataDir);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

//	public void setMaxCacheAge(int maxCacheAge) {
//		server.setMaxCacheAge(maxCacheAge);
//	}

	/**
	 * @throws StoreException
	 */
	private void createTestRepositories()
		throws StoreException, StoreConfigException
	{
		RepositoryManager manager = server.getRepositoryManager();

		// create a (non-inferencing) memory store
		MemoryStoreConfig memStoreConfig = new MemoryStoreConfig();
		SailRepositoryConfig sailRepConfig = new SailRepositoryConfig(memStoreConfig);
		RepositoryConfig repConfig = new RepositoryConfig(TEST_REPO_ID, sailRepConfig);

		manager.addRepositoryConfig(TEST_REPO_ID, repConfig.export());

		// create an inferencing memory store
		ForwardChainingRDFSInferencerConfig inferMemStoreConfig = new ForwardChainingRDFSInferencerConfig(
				new MemoryStoreConfig());
		sailRepConfig = new SailRepositoryConfig(inferMemStoreConfig);
		repConfig = new RepositoryConfig(TEST_INFERENCE_REPO_ID, sailRepConfig);

		manager.addRepositoryConfig(TEST_INFERENCE_REPO_ID, repConfig.export());
	}
}
