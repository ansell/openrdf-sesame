/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.server.rest;

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

import org.openrdf.repository.RepositoryManager;
import org.openrdf.repository.config.RepositoryManagerConfig;
import org.openrdf.repository.config.RepositoryManagerConfigReader;
import org.openrdf.repository.config.RepositoryManagerConfigWriter;
import org.openrdf.util.log.ThreadLog;
import org.xml.sax.SAXException;

/**
 * Servlet that takes care of the initialization and destruction of the
 * {@link RepositoryManager#getDefaultServer default server object}. It also handles
 * exports and submits of server configuration files to enable external
 * manipulation of the configuration file on a live server.
 */
public class RepositoryManagerConfigServlet extends HttpServlet {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = 2257507582006658535L;

	/*-----------*
	 * Variables *
	 *-----------*/

	private File _serverConfigFile;

	/*---------*
	 * Methods *
	 *---------*/

	// Implements Servlet.init(...)
	public final void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);

		// Log errors and warning to stderr until the system configuration has
		// been read
		ThreadLog.setDefaultLog(null, ThreadLog.STATUS);

		// Set base dir
		String baseDir = config.getServletContext().getRealPath("");
		RepositoryManager.setBaseDir(new File(baseDir));
		ThreadLog.log("Base dir is: " + baseDir);

		File logDir = RepositoryManager.resolvePath("WEB-INF/logs");
		int logLevel = ThreadLog.WARNING;

		// Read server configuration file
		String filePath = config.getInitParameter("serverConfigFile");
		if (filePath == null) {
			ThreadLog.error("'serverConfigFile' PARAMETER MISSING FROM INIT PARAMETERS");
		}
		else {
			_serverConfigFile = RepositoryManager.resolvePath(filePath);

			if (!_serverConfigFile.exists()) {
				ThreadLog.warning("SERVER CONFIGURATION FILE NOT FOUND: " + _serverConfigFile);
			}
			else if (!_serverConfigFile.canRead()) {
				ThreadLog.warning("CANNOT READ SERVER CONFIGURATION FILE: " + _serverConfigFile);
			}
			else {
				// configuration file should exists and be readable
				try {
					RepositoryManagerConfig sc = RepositoryManagerConfigReader.read(_serverConfigFile);

					ThreadLog.log("Server configuration read from: " + _serverConfigFile);

					RepositoryManager.getDefaultServer().setServerConfig(sc);

					logDir = RepositoryManager.resolvePath(sc.getLogDir());
					logLevel = sc.getLogLevel();
				}
				catch (SAXException e) {
					ThreadLog.error("FAILED TO READ SERVER CONFIGURATION FILE", e);
				}
				catch (IOException e) {
					ThreadLog.error("FAILED TO READ SERVER CONFIGURATION FILE", e);
				}
			}
		}

		ThreadLog.log("Log directory is: " + logDir);
		String logFile = new File(logDir, "general.log").getPath();
		ThreadLog.setDefaultLog(logFile, logLevel);
	}

	// Implements Servlet.destroy()
	public final void destroy() {
		RepositoryManager.getDefaultServer().clear();
		ThreadLog.unsetDefaultLog();
	}

	// Overrides HttpServlet.doGet(...)
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		try {
			ThreadLog.log("=== sending server configuration ===");

			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/xml");
			OutputStream out = response.getOutputStream();

			try {
				RepositoryManagerConfig serverConfig = RepositoryManager.getDefaultServer().getServerConfig();
				RepositoryManagerConfigWriter writer = new RepositoryManagerConfigWriter();
				writer.write(serverConfig, out);
			}
			finally {
				out.close();
			}

			ThreadLog.log("=== Server configuration sent ===");
		}
		catch (Exception e) {
			ThreadLog.error("FAILED TO SEND SERVER CONFIGURATION", e);
			response.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}

	protected void doPut(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		try {
			ThreadLog.log("=== receiving new server configuration ===");

			InputStream dataStream = request.getInputStream();
			try {
				RepositoryManagerConfig serverConfig = RepositoryManagerConfigReader.read(dataStream);

				ThreadLog.trace("Server configuration read successfully");

				if (_serverConfigFile != null) {
					OutputStream out = new FileOutputStream(_serverConfigFile);
					try {
						RepositoryManagerConfigWriter writer = new RepositoryManagerConfigWriter();
						writer.write(serverConfig, out);
						ThreadLog.trace("Server configuration stored");
					}
					finally {
						out.close();
					}
				}

				RepositoryManager.getDefaultServer().setServerConfig(serverConfig);
				ThreadLog.log("=== server configuration updated ===");

				// Indicate success with a 204 NO CONTENT response
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				response.getWriter().close();
			}
			finally {
				dataStream.close();
			}
		}
		catch (Exception e) {
			ThreadLog.error("FAILED TO PROCESS NEW SERVER CONFIGURATION", e);
			response.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}
}
