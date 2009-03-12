/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.helpers;

import javax.servlet.http.HttpServletRequest;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.manager.RepositoryManager;

/**
 * Class offering utility methods for getting/setting Sesame-specific request
 * attributes.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class RequestAtt {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final String BASE = "org.openrdf.sesame#";

	private static final String REPOSITORY_MANAGER = BASE + "repositoryManager";

	private static final String REPOSITORY_KEY = BASE + "repository";

	private static final String CONNECTION_KEY = BASE + "connection";

	private static final String ACTIVE_CON_KEY = BASE + "active-connection";

	/*---------*
	 * Methods *
	 *---------*/

	public static RepositoryManager getRepositoryManager(HttpServletRequest request) {
		return (RepositoryManager)request.getAttribute(REPOSITORY_MANAGER);
	}

	public static void setRepositoryManager(HttpServletRequest request, RepositoryManager manager) {
		request.setAttribute(REPOSITORY_MANAGER, manager);
	}

	public static Repository getRepository(HttpServletRequest request) {
		return (Repository)request.getAttribute(REPOSITORY_KEY);
	}

	public static void setRepository(HttpServletRequest request, Repository repository) {
		request.setAttribute(REPOSITORY_KEY, repository);
	}

	public static RepositoryConnection getRepositoryConnection(HttpServletRequest request) {
		return (RepositoryConnection)request.getAttribute(CONNECTION_KEY);
	}

	public static void setRepositoryConnection(HttpServletRequest request, RepositoryConnection con) {
		request.setAttribute(CONNECTION_KEY, con);
	}

	public static ActiveConnection getActiveConnection(HttpServletRequest request) {
		return (ActiveConnection)request.getAttribute(ACTIVE_CON_KEY);
	}

	public static void setActiveConnection(HttpServletRequest request, ActiveConnection activeCon) {
		request.setAttribute(ACTIVE_CON_KEY, activeCon);
	}
}
