/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.BlockingChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

public class EmbeddedServer {

	public static final String DEFAULT_HOST = "localhost";

	public static final int DEFAULT_PORT = 18080;

	public static final String DEFAULT_CONTEXT_PATH = "/";

	public static final String DEFAULT_WAR_PATH = "./src/main/webapp";

	private final Server jetty;

	public EmbeddedServer() {
		this(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_CONTEXT_PATH, DEFAULT_WAR_PATH);
	}

	public EmbeddedServer(String host, int port, String contextPath, String warPath) {
		System.clearProperty("DEBUG");

		jetty = new Server();

		Connector conn = new BlockingChannelConnector();
		conn.setHost(host);
		conn.setPort(port);
		jetty.addConnector(conn);

		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath(contextPath);
		webapp.setWar(warPath);
		jetty.addHandler(webapp);
	}

	public void start()
		throws Exception
	{
		jetty.start();
	}

	public void stop()
		throws Exception
	{
		jetty.stop();
		System.clearProperty("org.mortbay.log.class");
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args)
		throws Exception
	{
		EmbeddedServer server = new EmbeddedServer();
		server.start();
	}
}
