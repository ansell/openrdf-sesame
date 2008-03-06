/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import info.aduna.app.AppConfiguration;
import info.aduna.app.AppVersion;
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

	@Override
	public final void init()
		throws ServletException
	{
		String origThreadName = Thread.currentThread().getName();
		Thread.currentThread().setName("repositories:config");

		try {
			String applicationName = getServletConfig().getInitParameter("applicationName");
			AppConfiguration appConfig = new AppConfiguration(applicationName);
			appConfig.setVersion(new AppVersion(2, 0, "beta3"));
			appConfig.init();

			configureRepositories(appConfig.getDataDir());
		}
		catch (IOException ioe) {
			logger.error(ioe.getMessage(), ioe.getCause());
			throw new ServletException(ioe);
		}
		catch (ServletException e) {
			logger.error(e.getMessage(), e.getCause());
			throw e;
		}
		finally {
			Thread.currentThread().setName(origThreadName);
		}
	}

	/**
	 * Configure repositories from config file. Install default configuration on
	 * first use.
	 */
	private void configureRepositories(File dataDir)
		throws ServletException
	{
		RepositoryManager.setDataDir(dataDir);

		File repositoriesFile = RepositoryManager.resolvePath(CONFIG_FILE);

		if (!repositoriesFile.exists()) {
			logger.info("Repository configuration file not found: {}. Installing default...", repositoriesFile);
			installDefaultConfiguration(repositoriesFile);
		}

		readConfiguration(repositoriesFile);
	}

	/**
	 * Read repositories configuration from file.
	 * 
	 * @param configFile
	 *        the file from which to read the configuration
	 */
	private void readConfiguration(File configFile)
		throws ServletException
	{
		try {
			logger.debug("Loading repository configuration from: {}", configFile);
			RepositoryManagerConfig config = RepositoryManagerConfigReader.read(configFile);
			RepositoryManager.getDefaultInstance().setConfig(config);
		}
		catch (IOException e) {
			throw new ServletException("Failed to read repository configuration file: " + configFile, e);
		}
		catch (SAXException e) {
			throw new ServletException("Failed to parse repository configuration file: " + configFile, e);
		}
	}

	/**
	 * Install the default repositories configuration file.
	 * 
	 * @param configFile
	 *        the file in which to store the configuration. Will be overwritten.
	 * @throws IOException
	 *         if there was a problem reading the default or writing it to the
	 *         specified output.
	 */
	private void installDefaultConfiguration(File configFile)
		throws ServletException
	{
		try {
			configFile.getParentFile().mkdirs();
			InputStream in = this.getClass().getResourceAsStream("/default/" + CONFIG_FILE);
			OutputStream out = new FileOutputStream(configFile);
			IOUtil.transfer(in, out);
		}
		catch (IOException e) {
			throw new ServletException("Failed to write default repository configuration file: " + configFile, e);
		}
	}

	@Override
	public final void destroy()
	{
		RepositoryManager.getDefaultInstance().clear();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		String origThreadName = Thread.currentThread().getName();
		Thread.currentThread().setName("repositories:config");
		try {
			logger.info("Sending server configuration");

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

			logger.info("Server configuration sent");
		}
		catch (Exception e) {
			logger.error("Failed to send repositories configuration", e);
			response.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
		finally {
			Thread.currentThread().setName(origThreadName);
		}
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		String origThreadName = Thread.currentThread().getName();
		Thread.currentThread().setName("repositories:config");
		try {
			logger.info("Receiving new server configuration");

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
				logger.info("Server configuration updated");

				// Indicate success with a 204 NO CONTENT response
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				response.getWriter().close();
			}
			finally {
				dataStream.close();
			}
		}
		catch (Exception e) {
			logger.error("Failed to process new repositories configuration", e);
			response.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
		finally {
			Thread.currentThread().setName(origThreadName);
		}
	}
}
