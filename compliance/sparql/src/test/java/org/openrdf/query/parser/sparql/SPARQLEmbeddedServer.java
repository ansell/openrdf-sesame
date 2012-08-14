/*
 * Copyright fluid Operations AG (http://www.fluidops.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import java.io.File;
import java.util.List;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.BlockingChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.manager.SystemRepository;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.sail.memory.config.MemoryStoreConfig;

/**
 * An embedded http server for SPARQL query testing. Initializes a memory store
 * repository for each specified reposiotoryId.
 * 
 * @author Andreas Schwarte
 */
public class SPARQLEmbeddedServer {

	private static final String HOST = "localhost";

	private static final int PORT = 18080;

	private static final String OPENRDF_CONTEXT = "/openrdf";

	private final List<String> repositoryIds;

	private final Server jetty;

	/**
	 * @param repositoryIds
	 */
	public SPARQLEmbeddedServer(List<String> repositoryIds) {
		this.repositoryIds = repositoryIds;
		System.clearProperty("DEBUG");

		jetty = new Server();

		Connector conn = new BlockingChannelConnector();
		conn.setHost(HOST);
		conn.setPort(PORT);
		jetty.addConnector(conn);

		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath(OPENRDF_CONTEXT);
		// warPath configured in pom.xml maven-war-plugin configuration
		webapp.setWar("./target/openrdf-sesame");
		jetty.addHandler(webapp);
	}

	/**
	 * @return the url to the repository with given id
	 */
	public String getRepositoryUrl(String repoId) {
		return Protocol.getRepositoryLocation(getServerUrl(), repoId);
	}

	/**
	 * @return the server url
	 */
	public String getServerUrl() {
		return "http://" + HOST + ":" + PORT + OPENRDF_CONTEXT;
	}

	public void start()
		throws Exception
	{
		File dataDir = new File(System.getProperty("user.dir") + "/target/datadir");
		dataDir.mkdirs();
		System.setProperty("info.aduna.platform.appdata.basedir", dataDir.getAbsolutePath());

		jetty.start();

		createTestRepositories();
	}

	public void stop()
		throws Exception
	{
		Repository systemRepo = new HTTPRepository(Protocol.getRepositoryLocation(getServerUrl(),
				SystemRepository.ID));
		RepositoryConnection con = systemRepo.getConnection();
		try {
			con.clear();
		}
		finally {
			con.close();
			systemRepo.shutDown();
		}

		jetty.stop();
		System.clearProperty("org.mortbay.log.class");
	}

	private void createTestRepositories()
		throws RepositoryException, RepositoryConfigException
	{
		Repository systemRep = new HTTPRepository(Protocol.getRepositoryLocation(getServerUrl(),
				SystemRepository.ID));

		// create a memory store for each provided repository id
		for (String repId : repositoryIds) {
			MemoryStoreConfig memStoreConfig = new MemoryStoreConfig();
			memStoreConfig.setPersist(false);
			SailRepositoryConfig sailRepConfig = new SailRepositoryConfig(memStoreConfig);
			RepositoryConfig repConfig = new RepositoryConfig(repId, sailRepConfig);

			RepositoryConfigUtil.updateRepositoryConfigs(systemRep, repConfig);
		}

	}
}
