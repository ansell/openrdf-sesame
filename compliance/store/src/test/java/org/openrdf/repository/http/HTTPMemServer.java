/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.io.File;
import java.io.IOException;

import info.aduna.io.FileUtil;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.server.SesameServer;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.manager.templates.ConfigTemplate;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

/**
 * @author Herko ter Horst
 */
public class HTTPMemServer {

	public static final String TEST_REPO_ID = "memory";

	public static final String TEST_INFERENCE_REPO_ID = "memory-rdfs";

	public static int DEFAULT_PORT = 18080;

	public static String SERVER_URL = "http://localhost:" + DEFAULT_PORT;

	public static String REPOSITORY_URL = Protocol.getRepositoryLocation(HTTPMemServer.SERVER_URL,
			TEST_REPO_ID);

	public static String INFERENCE_REPOSITORY_URL = Protocol.getRepositoryLocation(HTTPMemServer.SERVER_URL,
			TEST_INFERENCE_REPO_ID);

	private final SesameServer server;

	private final File dataDir;

	public HTTPMemServer()
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

	public RepositoryManager getRepositoryManager() {
		return server.getRepositoryManager();
	}

	/**
	 * @throws StoreException
	 */
	private void createTestRepositories()
		throws StoreException, StoreConfigException
	{
		RepositoryManager manager = server.getRepositoryManager();
		ConfigTemplate memory = manager.getConfigTemplateManager().getTemplate("memory");
		manager.addRepositoryConfig(TEST_REPO_ID, memory.createConfig(null));
		ConfigTemplate memory_rdfs_dt = manager.getConfigTemplateManager().getTemplate("memory-rdfs");
		manager.addRepositoryConfig(TEST_INFERENCE_REPO_ID, memory_rdfs_dt.createConfig(null));
	}
}
