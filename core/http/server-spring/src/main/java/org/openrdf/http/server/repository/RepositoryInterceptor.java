/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import org.openrdf.http.server.exceptions.ClientHTTPException;
import org.openrdf.http.server.exceptions.ServerHTTPException;
import org.openrdf.http.server.helpers.ProtocolUtil;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

/**
 * Interceptor for repository requests. Handles the opening and closing of
 * connections to the repository specified in the request.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class RepositoryInterceptor extends HandlerInterceptorAdapter {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * 
	 */
	private static final String REPOSITORIES = "/repositories/";

	private static final String REPOSITORY_MANAGER = "repositoryManager";

	private static final String REPOSITORY_KEY = "repository";

	private static final String REPOSITORY_CONNECTION_KEY = "repositoryConnection";

	/*-----------*
	 * Variables *
	 *-----------*/

	private RepositoryManager repositoryManager;

	/*---------*
	 * Methods *
	 *---------*/

	public void setRepositoryManager(RepositoryManager repMan) {
		repositoryManager = repMan;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
		throws Exception
	{
		request.setAttribute(REPOSITORY_MANAGER, repositoryManager);
		String repositoryID = getRepositoryID(request);

		ProtocolUtil.logRequestParameters(request);

		if (repositoryID != null) {
			try {
				Repository repository = repositoryManager.getRepository(repositoryID);

				if (repository == null) {
					throw new ClientHTTPException(SC_NOT_FOUND, "Unknown repository: " + repositoryID);
				}

				RepositoryConnection repositoryCon = repository.getConnection();
				request.setAttribute(REPOSITORY_KEY, repository);
				request.setAttribute(REPOSITORY_CONNECTION_KEY, repositoryCon);
			}
			catch (StoreConfigException e) {
				throw new ServerHTTPException(e.getMessage(), e);
			}
			catch (StoreException e) {
				throw new ServerHTTPException(e.getMessage(), e);
			}
		}
		return super.preHandle(request, response, handler);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception exception)
		throws ServerHTTPException
	{
		RepositoryConnection repositoryCon = getRepositoryConnection(request);
		if (repositoryCon != null) {
			try {
				repositoryCon.close();
			}
			catch (StoreException e) {
				throw new ServerHTTPException(e.getMessage(), e);
			}
		}
	}

	public static RepositoryManager getRepositoryManager(HttpServletRequest request) {
		return (RepositoryManager)request.getAttribute(REPOSITORY_MANAGER);
	}

	public static String getRepositoryID(HttpServletRequest request) {
		String path = request.getRequestURI();
		int start = path.indexOf(REPOSITORIES);
		if (start < 0) {
			return null;
		}
		String id = path.substring(start + REPOSITORIES.length());
		if (id.contains("/")) {
			id = id.substring(0, id.indexOf('/'));
		}
		return id;
	}

	public static Repository getRepository(HttpServletRequest request) {
		return (Repository)request.getAttribute(REPOSITORY_KEY);
	}

	public static RepositoryConnection getRepositoryConnection(HttpServletRequest request) {
		return (RepositoryConnection)request.getAttribute(REPOSITORY_CONNECTION_KEY);
	}
}
