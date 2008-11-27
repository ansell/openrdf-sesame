/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.io.File;
import java.io.IOException;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import info.aduna.io.FileUtil;

import org.openrdf.http.server.SesameServlet;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.store.StoreConfigException;

public class EmbeddedServer {

	public static int DEFAULT_PORT = 8080;

	private File dataDir;

	private Server jetty;

	private RepositoryManager manager;

	private SesameServlet servlet;

	public EmbeddedServer()
		throws IOException, StoreConfigException
	{
		this(DEFAULT_PORT);
	}

	public EmbeddedServer(int port)
		throws IOException, StoreConfigException
	{
		System.setProperty("org.mortbay.log.class", "org.mortbay.log.StdErrLog");
		System.clearProperty("DEBUG");
		dataDir = FileUtil.createTempDir("server");
		manager = new LocalRepositoryManager(dataDir);
		manager.initialize();
		jetty = new Server(port);
		servlet = new SesameServlet(manager);
		Context root = new Context(jetty, "/");
		root.addServlet(new ServletHolder(servlet), "/*");
	}

	public void setMaxCacheAge(int maxCacheAge) {
		servlet.setMaxCacheAge(maxCacheAge);
	}

	public RepositoryManager getRepositoryManager() {
		return manager;
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
		FileUtil.deleteDir(dataDir);
	}

	public static void main(String[] args)
		throws IOException, StoreConfigException
	{
		EmbeddedServer server = new EmbeddedServer();
		try {
			server.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
