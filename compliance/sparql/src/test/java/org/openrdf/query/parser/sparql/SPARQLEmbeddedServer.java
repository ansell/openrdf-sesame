/*
 * Copyright fluid Operations AG (http://www.fluidops.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import java.io.File;
import java.util.List;

import info.aduna.net.http.server.embedded.EmbeddedServer;

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
public class SPARQLEmbeddedServer extends EmbeddedServer {

	protected static final String OPENRDF_CONTEXT = "/openrdf";
	protected static final String OPENRDF_SERVER_WAR = "./target/openrdf-sesame";
		
	protected final List<String> repositoryIds;
	
	
	/**
	 * @param repositoryIds
	 */
	public SPARQLEmbeddedServer(List<String> repositoryIds) {
		// warPath configured in pom.xml maven-war-plugin configuration
		super(DEFAULT_HOST, DEFAULT_PORT, OPENRDF_CONTEXT, OPENRDF_SERVER_WAR);
		this.repositoryIds = repositoryIds;
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
		return "http://" + DEFAULT_HOST + ":" + DEFAULT_PORT + OPENRDF_CONTEXT;
	}
	
	
	@Override
	public void start()
		throws Exception
	{
		File dataDir = new File(System.getProperty("user.dir") + "/target/datadir");
		dataDir.mkdirs();
		System.setProperty("info.aduna.platform.appdata.basedir", dataDir.getAbsolutePath());

		super.start();

		createTestRepositories();
	}
	

	@Override
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
		}

		super.stop();
	}

	/**
	 * @throws RepositoryException
	 */
	private void createTestRepositories()
		throws RepositoryException, RepositoryConfigException
	{
		Repository systemRep = new HTTPRepository(Protocol.getRepositoryLocation(getServerUrl(),
				SystemRepository.ID));

		// create a memory store for each provided repository id
		for (String repId : repositoryIds) {
			MemoryStoreConfig memStoreConfig = new MemoryStoreConfig();
			SailRepositoryConfig sailRepConfig = new SailRepositoryConfig(memStoreConfig);
			RepositoryConfig repConfig = new RepositoryConfig(repId, sailRepConfig);
	
			RepositoryConfigUtil.updateRepositoryConfigs(systemRep, repConfig);
		}

	}
}
