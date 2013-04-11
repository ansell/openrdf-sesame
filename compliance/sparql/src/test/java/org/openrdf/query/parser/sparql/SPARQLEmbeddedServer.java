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
package org.openrdf.query.parser.sparql;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

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

	private static final String OPENRDF_CONTEXT = "/openrdf";

	private final int port;

	private final List<String> repositoryIds;

	private final Server jetty;

	private final String serverUrl;

	private WebAppContext webapp;

	/**
	 * @param repositoryIds
	 */
	public SPARQLEmbeddedServer(List<String> repositoryIds, Path testDir) {
		this.repositoryIds = repositoryIds;
		System.clearProperty("DEBUG");

		port = getFreePort();
		serverUrl = "http://" + HOST + ":" + port + OPENRDF_CONTEXT;
		jetty = new Server();
		jetty.setAttribute("info.aduna.platform.appdata.basedir", testDir.toAbsolutePath().toString());

		ServerConnector conn = new ServerConnector(jetty);
		conn.setHost(HOST);
		conn.setPort(port);
		// conn.setSoLingerTime(-1);
		jetty.addConnector(conn);

		webapp = new WebAppContext();
		webapp.setContextPath(OPENRDF_CONTEXT);
		// warPath configured in pom.xml maven-war-plugin configuration
		webapp.setWar("./target/openrdf-sesame.war");
		jetty.setHandler(webapp);
		webapp.setAttribute("info.aduna.platform.appdata.basedir", testDir.toAbsolutePath().toString());

	}

	private static int getFreePort() {
		int result = -1;
		try (ServerSocket ss = new ServerSocket(0)) {
			ss.setReuseAddress(true);
			result = ss.getLocalPort();
			try (DatagramSocket ds = new DatagramSocket(result);) {
				ds.setReuseAddress(true);
			}
		}
		catch (IOException e) {
			result = -1;
		}

		return result;
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
		return serverUrl;
	}

	public void start()
		throws Exception
	{
		// File dataDir = new File(System.getProperty("user.dir") +
		// "/target/datadir");
		// dataDir.mkdirs();
		// System.setProperty("info.aduna.platform.appdata.basedir",
		// dataDir.getAbsolutePath());

		jetty.start();

		createTestRepositories();
	}

	public void stop()
		throws Exception
	{
		try {
			Repository systemRepo = new HTTPRepository(Protocol.getRepositoryLocation(getServerUrl(),
					SystemRepository.ID));
			if (systemRepo != null) {
				try (RepositoryConnection con = systemRepo.getConnection();) {
					con.clear();
				}
				finally {
					systemRepo.shutDown();
				}
			}
		}
		finally {
			try {
				webapp.stop();
			}
			finally {
				try {
					webapp.destroy();
				}
				finally {
					try {
						jetty.stop();
					}
					finally {
						jetty.destroy();
					}
				}
			}
		}
		// System.out.println("server stopped at: " + serverUrl);
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
