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
package org.openrdf.http.server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

//import org.mortbay.jetty.Connector;
//import org.mortbay.jetty.Server;
//import org.mortbay.jetty.nio.BlockingChannelConnector;
//import org.mortbay.jetty.webapp.WebAppContext;

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
import org.openrdf.sail.inferencer.fc.config.ForwardChainingRDFSInferencerConfig;
import org.openrdf.sail.memory.config.MemoryStoreConfig;

/**
 * @author Herko ter Horst
 */
public class TestServer {

	private static final String HOST = "localhost";

	private static final String TEST_REPO_ID = "Test";

	private static final String TEST_INFERENCE_REPO_ID = "Test-RDFS";

	private static final String OPENRDF_CONTEXT = "/openrdf";

	private final int port;

	private final String serverUrl;

	private final String repositoryUrl;

	private final Server jetty;

	private WebAppContext webapp;

	private File dataDir;

	public TestServer(File dataDir) {
		System.clearProperty("DEBUG");

		this.dataDir = dataDir;

		port = getFreePort();
		serverUrl = "http://" + HOST + ":" + port + OPENRDF_CONTEXT;
		repositoryUrl = Protocol.getRepositoryLocation(serverUrl, TEST_REPO_ID);
		jetty = new Server();
		jetty.setAttribute("info.aduna.platform.appdata.basedir", dataDir.getAbsolutePath());

		ServerConnector conn = new ServerConnector(jetty);
		conn.setHost(HOST);
		conn.setPort(port);
		jetty.addConnector(conn);

		webapp = new WebAppContext();
		webapp.setAttribute("info.aduna.platform.appdata.basedir", dataDir.getAbsolutePath());
		System.out.println("setting webapp data dir attribute to: " + dataDir.getAbsolutePath());
		webapp.setContextPath(OPENRDF_CONTEXT);
		// warPath configured in pom.xml maven-war-plugin configuration
		webapp.setWar("./target/openrdf-sesame.war");
		jetty.setHandler(webapp);
		jetty.setAttribute("info.aduna.platform.appdata.basedir", dataDir.getAbsolutePath());
		webapp.setAttribute("info.aduna.platform.appdata.basedir", dataDir.getAbsolutePath());

	}

	/**
	 * Checks to see if a specific port is available.
	 * 
	 * @param port
	 *        the port to check for availability
	 */
	private static int getFreePort() {
		int result = -1;
		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(0);
			ss.setReuseAddress(true);
			result = ss.getLocalPort();
			try {
				ds = new DatagramSocket(result);
				ds.setReuseAddress(true);
			}
			finally {
				if (ds != null) {
					ds.close();
				}
			}
		}
		catch (IOException e) {
		}
		finally {
			if (ss != null) {
				try {
					ss.close();
				}
				catch (IOException e) {
					/* should not be thrown */
				}
			}
		}

		return result;
	}

	public void start()
		throws Exception
	{
		// System.setProperty("info.aduna.platform.appdata.basedir",
		// dataDir.getAbsolutePath());

		jetty.start();
		System.out.println("server started at: " + serverUrl);
		jetty.setAttribute("info.aduna.platform.appdata.basedir", dataDir.getAbsolutePath());
		webapp.setAttribute("info.aduna.platform.appdata.basedir", dataDir.getAbsolutePath());

		createTestRepositories();
	}

	public void stop()
		throws Exception
	{
		Repository systemRepo = new HTTPRepository(Protocol.getRepositoryLocation(serverUrl,
				SystemRepository.ID));
		RepositoryConnection con = systemRepo.getConnection();
		try {
			con.clear();
		}
		finally {
			con.close();
		}

		jetty.stop();
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
	 * @return Returns the repository URL.
	 */
	public String getRepositoryUrl() {
		return repositoryUrl;
	}

}
