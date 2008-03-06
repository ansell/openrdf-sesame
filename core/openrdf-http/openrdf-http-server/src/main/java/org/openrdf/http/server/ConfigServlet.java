/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import info.aduna.app.config.AppConfiguration;
import info.aduna.io.IOUtil;

import org.openrdf.repository.config.RepositoryManager;
import org.openrdf.repository.config.RepositoryManagerConfig;
import org.openrdf.repository.config.RepositoryManagerConfigReader;
import org.openrdf.repository.config.RepositoryManagerConfigWriter;

/**
 * Servlet that takes care of the initialization and destruction of the
 * {@link RepositoryManager#getDefaultServer default server object}. It also
 * handles exports and submits of server configuration files to enable external
 * manipulation of the configuration file on a live server.
 */
public class ConfigServlet extends HttpServlet {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = 2257507582006658535L;

	private static final String CONFIG_FILE = "repositories.xml";

	/*---------*
	 * Methods *
	 *---------*/

	public final void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);

		String oldName = Thread.currentThread().getName();
		Thread.currentThread().setName("repositories:config");

		String applicationName = config.getInitParameter("applicationName");
		AppConfiguration appConfig = AppConfiguration.getInstance(applicationName);

		try {
			configureRepositories(appConfig.getDataDir());
		}
		finally {
			Thread.currentThread().setName(oldName);
		}
	}

	/**
	 * Configure repositories from config file. Install default configuration on
	 * first use.
	 */
	private void configureRepositories(File dataDir) {
		RepositoryManager.setDataDir(dataDir);

		File repositoriesFile = RepositoryManager.resolvePath(CONFIG_FILE);

		if (!repositoriesFile.exists()) {
			logger.info("Repositories configuration file not found: {}. Installing default...", repositoriesFile);
			try {
				installDefaultConfiguration(repositoriesFile);
				readConfiguration(repositoriesFile);
			}
			catch (IOException e) {
				logger.error("Unable to write or read default repositories configuration: {}", repositoriesFile);
			}
		}
		else if (!repositoriesFile.canRead()) {
			logger.error("Unable to read repositories configuration: {}", repositoriesFile);
		}
		else {
			readConfiguration(repositoriesFile);
		}
	}

	/**
	 * Read repositories configuration from file.
	 * 
	 * @param repositoriesFile
	 *        the file from which to read the configuration
	 */
	private void readConfiguration(File repositoriesFile) {
		try {
			logger.debug("Loading repositories configuration from: {}", repositoriesFile);
			RepositoryManagerConfig config = RepositoryManagerConfigReader.read(repositoriesFile);
			RepositoryManager.getDefaultInstance().setConfig(config);
		}
		catch (SAXException e) {
			logger.error("Failed to read repositories configuration file", e);
		}
		catch (IOException e) {
			logger.error("Failed to read repositories configuration file", e);
		}
	}

	/**
	 * Install the default repositories configuration file.
	 * 
	 * @param repositoriesFile
	 *        the file in which to store the configuration. Will be overwritten.
	 * @throws IOException
	 *         if there was a problem reading the default or writing it to the
	 *         specified output.
	 */
	private void installDefaultConfiguration(File repositoriesFile)
		throws IOException
	{
		repositoriesFile.getParentFile().mkdirs();
		InputStream in = this.getClass().getResourceAsStream("/default/" + CONFIG_FILE);
		OutputStream out = new FileOutputStream(repositoriesFile);
		IOUtil.transfer(in, out);
	}

	// Implements Servlet.destroy()
	public final void destroy() {
		RepositoryManager.getDefaultInstance().clear();
	}

	// Overrides HttpServlet.doGet(...)
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		String oldName = Thread.currentThread().getName();
		Thread.currentThread().setName("repositories:config");
		try {
			try {
				logger.info("=== sending server configuration ===");

				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("application/xml");
				OutputStream out = response.getOutputStream();

				try {
					RepositoryManagerConfig serverConfig = RepositoryManager.getDefaultInstance().getConfig();
					RepositoryManagerConfigWriter writer = new RepositoryManagerConfigWriter();
					writer.write(serverConfig, out);
				}
				finally {
					out.close();
				}

				logger.info("=== Server configuration sent ===");
			}
			catch (Exception e) {
				logger.error("Unable to send repositories configuration", e);
				response.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
		}
		finally {
			Thread.currentThread().setName(oldName);
		}
	}

	protected void doPut(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		String oldName = Thread.currentThread().getName();
		Thread.currentThread().setName("repositories:config");
		try {
			try {
				logger.info("=== receiving new server configuration ===");

				InputStream dataStream = request.getInputStream();
				try {
					RepositoryManagerConfig config = RepositoryManagerConfigReader.read(dataStream);

					logger.debug("Server configuration read successfully");

					File configFile = RepositoryManager.resolvePath(CONFIG_FILE);
					OutputStream out = new FileOutputStream(configFile);
					try {
						RepositoryManagerConfigWriter writer = new RepositoryManagerConfigWriter();
						writer.write(config, out);
						logger.debug("Server configuration stored");
					}
					finally {
						out.close();
					}

					RepositoryManager.getDefaultInstance().setConfig(config);
					logger.info("=== server configuration updated ===");

					// Indicate success with a 204 NO CONTENT response
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					response.getWriter().close();
				}
				finally {
					dataStream.close();
				}
			}
			catch (Exception e) {
				logger.error("Unable to process new repositories configuration", e);
				response.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
		}
		finally {
			Thread.currentThread().setName(oldName);
		}
	}
}
