/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.store.StoreConfigException;

/**
 * Stand alone server for Sesame.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public class SesameServer {

	@Deprecated
	public static void main(String[] args)
		throws Exception
	{
		System.err.println("Class to start Sesame server has changed, please use org.openrdf.http.server.Start");
		Start.main(args);
	}

	private static Random random = new Random(System.currentTimeMillis());

	public static final int DEFAULT_PORT = 8080;

	public static final String SHUTDOWN_PATH = "shutdown";
	
	public static final String KEY_PARAM = "key";

	private final Logger logger = LoggerFactory.getLogger(SesameServer.class);

	private final Server jetty;

	private final LocalRepositoryManager manager;

	private final SesameServlet servlet;

	private String shutDownKey;

	/**
	 * Creates a new Sesame server that listens to the default port number (
	 * <tt>8080</tt>).
	 * 
	 * @param dataDir
	 *        The data directory for the server.
	 * @throws IOException
	 * @throws StoreConfigException
	 */
	public SesameServer(File dataDir)
		throws IOException, StoreConfigException
	{
		this(dataDir, DEFAULT_PORT);
	}

	public SesameServer(File dataDir, int port)
		throws IOException, StoreConfigException
	{
		assert dataDir != null : "dataDir must not be null";

		manager = new LocalRepositoryManager(dataDir);
		manager.initialize();

		servlet = new SesameServlet(manager);
		jetty = new Server(port);
		// jetty.setGracefulShutdown(30 * 1000);

		setShutdownKey(String.valueOf(random.nextLong()));
	}

	public File getDataDir() {
		return manager.getBaseDir();
	}

	public void setMaxCacheAge(int maxCacheAge) {
		servlet.setMaxCacheAge(maxCacheAge);
	}

	public void setShutdownKey(String shutdownKey) {
		if (shutdownKey == null) {
			throw new IllegalArgumentException("shutdownKey must not be null");
		}

		this.shutDownKey = shutdownKey;
	}

	public String getShutdownKey() {
		return shutDownKey;
	}

	public RepositoryManager getRepositoryManager() {
		return manager;
	}

	public void start()
		throws Exception
	{
		Context root = new Context(jetty, "/");
		root.setMaxFormContentSize(0);
		root.addServlet(new ServletHolder(new ShutdownHandler()), "/" + SHUTDOWN_PATH);
		root.addServlet(new ServletHolder(servlet), "/*");

		jetty.start();
	}

	public void stop()
		throws Exception
	{
		jetty.stop();
	}

	private class ShutdownHandler extends HttpServlet {

		private static final long serialVersionUID = -4103337674521311088L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
		{
			String key = req.getParameter(KEY_PARAM);
			if (getShutdownKey().equals(key)) {
				resp.setStatus(HttpServletResponse.SC_ACCEPTED);
				resp.getWriter().close();
				scheduleShutdown();
			}
			else {
				resp.sendError(HttpServletResponse.SC_FORBIDDEN, "invalid shutdown key");
			}
		}

		private void scheduleShutdown() {
			Thread t = new Thread("Server shut down thread") {

				public void run() {
					try {
						SesameServer.this.stop();
					}
					catch (Exception e) {
						logger.error("Failed to stop server", e);
					}
				}
			};
			t.setDaemon(true);
			t.start();
		}
	}
}
