/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import java.io.File;

import info.aduna.net.http.server.embedded.EmbeddedServer;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.manager.SystemRepository;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.sail.inferencer.fc.config.ForwardChainingRDFSInferencerConfig;
import org.openrdf.sail.memory.config.MemoryStoreConfig;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

/**
 * @author Herko ter Horst
 */
public class TestServer extends EmbeddedServer {

	public static final String TEST_REPO_ID = "Test";

	public static final String TEST_INFERENCE_REPO_ID = "Test-RDFS";

	public static final String OPENRDF_CONTEXT = "/openrdf";

	public static String SERVER_URL = "http://" + DEFAULT_HOST + ":" + DEFAULT_PORT + OPENRDF_CONTEXT;

	public static String REPOSITORY_URL = Protocol.getRepositoryLocation(TestServer.SERVER_URL, TEST_REPO_ID);

	public static String INFERENCE_REPOSITORY_URL = Protocol.getRepositoryLocation(TestServer.SERVER_URL,
			TEST_INFERENCE_REPO_ID);

	public static final String OPENRDF_SERVER_WAR = "./target/openrdf-sesame";

	public TestServer() {
		// warPath configured in pom.xml maven-war-plugin configuration
		super(DEFAULT_HOST, DEFAULT_PORT, OPENRDF_CONTEXT, OPENRDF_SERVER_WAR);
	}

	@Override
	public void start()
		throws Exception
	{
		File dataDir = new File(System.getProperty("user.dir") + "/target/datadir");
		dataDir.mkdirs();
		System.setProperty("info.aduna.platform.appdata.basedir", dataDir.getAbsolutePath());
//		System.setProperty("DEBUG", "true");

		super.start();

		createTestRepositories();
	}

	@Override
	public void stop()
		throws Exception
	{
		Repository systemRepo = new HTTPRepository(Protocol.getRepositoryLocation(SERVER_URL,
				SystemRepository.ID));
		RepositoryConnection con = systemRepo.getConnection();
		try {
			con.clear();
		}
		finally {
			con.close();
		}

		super.stop();
	}

	/**
	 * @throws StoreException
	 */
	private void createTestRepositories()
		throws StoreException, StoreConfigException
	{
		Repository systemRep = new HTTPRepository(Protocol.getRepositoryLocation(SERVER_URL,
				SystemRepository.ID));

		// create a (non-inferencing) memory store
		MemoryStoreConfig memStoreConfig = new MemoryStoreConfig();
		SailRepositoryConfig sailRepConfig = new SailRepositoryConfig(memStoreConfig);
		RepositoryConfig repConfig = new RepositoryConfig(TEST_REPO_ID, sailRepConfig);

		RepositoryConfigUtil.updateRepositoryConfigs(systemRep, repConfig);

		// create an inferencing memory store
		ForwardChainingRDFSInferencerConfig inferMemStoreConfig = new ForwardChainingRDFSInferencerConfig(
				new MemoryStoreConfig());
		sailRepConfig = new SailRepositoryConfig(inferMemStoreConfig);
		repConfig = new RepositoryConfig(TEST_INFERENCE_REPO_ID, sailRepConfig);

		RepositoryConfigUtil.updateRepositoryConfigs(systemRep, repConfig);
	}
}
