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
package org.openrdf.repository.http;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
import org.openrdf.repository.manager.SystemRepository;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.sail.inferencer.fc.config.ForwardChainingRDFSInferencerConfig;
import org.openrdf.sail.memory.config.MemoryStoreConfig;

/**
 * @author Herko ter Horst
 */
public class HTTPMemServer {

	private static final String HOST = "localhost";

	private static final String TEST_REPO_ID = "Test";

	private static final String TEST_INFERENCE_REPO_ID = "Test-RDFS";

	private static final String OPENRDF_CONTEXT = "/openrdf";

	private static final Set<Integer> usedPorts = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());

	private int port;

	private final String serverUrl;

	private final String repositoryUrl;

	private Server jetty;

	private final String inferenceRepositoryUrl;

	private WebAppContext webapp;

	public HTTPMemServer(Path testDir) {
		System.clearProperty("DEBUG");

		port = getFreePort();

		serverUrl = "http://" + HOST + ":" + port + OPENRDF_CONTEXT;
		repositoryUrl = Protocol.getRepositoryLocation(serverUrl, TEST_REPO_ID);
		inferenceRepositoryUrl = Protocol.getRepositoryLocation(serverUrl, TEST_INFERENCE_REPO_ID);
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

	private static synchronized int getFreePort() {
		int result = -1;
		while (result <= 0) {
			try (ServerSocket ss = new ServerSocket(0)) {
				ss.setReuseAddress(true);
				result = ss.getLocalPort();
				if(usedPorts.contains(result)) {
					result = -1;
				}
				else
				{
					usedPorts.add(result);
					try (DatagramSocket ds = new DatagramSocket(result);) {
						ds.setReuseAddress(true);
					}
				}
			}
			catch (IOException e) {
				result = -1;
			}
		}
		return result;
	}

	public void start()
		throws Exception
	{
		// System.setProperty("info.aduna.platform.appdata.basedir",
		// dataDir.getAbsolutePath());

		// System.out.println("about to start server at: " + serverUrl);
		jetty.start();
		// System.out.println("server started at: " + serverUrl);

		createTestRepositories();
	}

	public void stop()
		throws Exception
	{
		try {
			Repository systemRepo = new HTTPRepository(Protocol.getRepositoryLocation(serverUrl,
					SystemRepository.ID));
			if (systemRepo != null) {
				try (RepositoryConnection con = systemRepo.getConnection();) {
					con.clear();
				}
				catch (RepositoryException e) {
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
		Repository systemRep = new HTTPRepository(
				Protocol.getRepositoryLocation(serverUrl, SystemRepository.ID));

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

	/**
	 * @return Returns the repositoryUrl.
	 */
	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	/**
	 * @return Returns the inferenceRepositoryUrl.
	 */
	public String getInferenceRepositoryUrl() {
		return inferenceRepositoryUrl;
	}
}
