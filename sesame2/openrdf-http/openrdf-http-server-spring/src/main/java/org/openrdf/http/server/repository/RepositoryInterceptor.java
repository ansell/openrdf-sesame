/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.server.ClientHTTPException;
import org.openrdf.http.server.ServerHTTPException;
import org.openrdf.http.server.ProtocolUtil;
import org.openrdf.http.server.ServerInterceptor;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.LocalRepositoryManager;

/**
 * Interceptor for repository requests. Handles the opening and closing of
 * connections to the repository specified in the request. Should not be a
 * singleton bean! Configure as inner bean in openrdf-servlet.xml
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class RepositoryInterceptor extends ServerInterceptor {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String REPOSITORY_ID_KEY = "repositoryID";

	private static final String REPOSITORY_KEY = "repository";

	private static final String REPOSITORY_CONNECTION_KEY = "repositoryConnection";

	/*-----------*
	 * Variables *
	 *-----------*/
	
	private LocalRepositoryManager repositoryManager;

	private String repositoryID;

	private RepositoryConnection repositoryCon;

	/*---------*
	 * Methods *
	 *---------*/

	public void setRepositoryManager(LocalRepositoryManager repMan) {
		repositoryManager = repMan;
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse respons, Object handler)
		throws Exception
	{
		String pathInfoStr = request.getPathInfo();
		logger.debug("path info: {}", pathInfoStr);

		repositoryID = null;

		if (pathInfoStr != null && !pathInfoStr.equals("/")) {
			String[] pathInfo = pathInfoStr.substring(1).split("/");
			if (pathInfo.length > 0) {
				repositoryID = pathInfo[0];
				logger.debug("repositoryID is '{}'", repositoryID);
			}
		}

		ProtocolUtil.logRequestParameters(request);

		return super.preHandle(request, respons, handler);
	}

	@Override
	protected String getThreadName()
	{
		String threadName = Protocol.REPOSITORIES;

		if (repositoryID != null) {
			threadName += "/" + repositoryID;
		}

		return threadName;
	}

	@Override
	protected void setRequestAttributes(HttpServletRequest request)
		throws ClientHTTPException, ServerHTTPException
	{
		if (repositoryID != null) {
			try {
				Repository repository = repositoryManager.getRepository(repositoryID);

				if (repository == null) {
					throw new ClientHTTPException(SC_NOT_FOUND, "Unknown repository: " + repositoryID);
				}

				repositoryCon = repository.getConnection();
				request.setAttribute(REPOSITORY_ID_KEY, repositoryID);
				request.setAttribute(REPOSITORY_KEY, repository);
				request.setAttribute(REPOSITORY_CONNECTION_KEY, repositoryCon);
			}
			catch (RepositoryConfigException e) {
				throw new ServerHTTPException(e.getMessage(), e);
			}
			catch (RepositoryException e) {
				throw new ServerHTTPException(e.getMessage(), e);
			}
		}
	}

	@Override
	protected void cleanUpResources()
		throws ServerHTTPException
	{
		if (repositoryCon != null) {
			try {
				repositoryCon.close();
			}
			catch (RepositoryException e) {
				throw new ServerHTTPException(e.getMessage(), e);
			}
		}
	}

	public static String getRepositoryID(HttpServletRequest request) {
		return (String)request.getAttribute(REPOSITORY_ID_KEY);
	}

	public static Repository getRepository(HttpServletRequest request) {
		return (Repository)request.getAttribute(REPOSITORY_KEY);
	}

	public static RepositoryConnection getRepositoryConnection(HttpServletRequest request) {
		return (RepositoryConnection)request.getAttribute(REPOSITORY_CONNECTION_KEY);
	}
}
