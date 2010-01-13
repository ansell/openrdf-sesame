/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources.helpers;

import org.openrdf.http.server.SesameApplication;
import org.openrdf.http.server.helpers.CacheInfo;
import org.openrdf.http.server.helpers.RequestAtt;
import org.openrdf.http.server.helpers.ServerConnection;
import org.openrdf.http.server.helpers.ServerRepository;
import org.openrdf.http.server.helpers.ServerRepositoryManager;

/**
 * @author Arjohn Kampman
 */
public abstract class SesameResource extends CacheableResource {

	public SesameResource() {
		// Currently, we don't use method annotations in Sesame
		setAnnotated(false);
	}

	protected CacheInfo getCacheInfo() {
		ServerConnection connection = getConnection();
		if (connection != null) {
			return connection.getCacheInfo();
		}

		ServerRepository repository = getRepository();
		if (repository != null) {
			return repository.getCacheInfo();
		}

		return null;
	}

	@Override
	public SesameApplication getApplication() {
		return (SesameApplication)super.getApplication();
	}

	/**
	 * Short-cut for <tt>getApplication().getRepositoryManager()</tt>.
	 */
	protected ServerRepositoryManager getRepositoryManager() {
		return getApplication().getServerRepositoryManager();
	}

	/**
	 * Gets the repository from the request's attributes, if available.
	 * 
	 * @return The repository stored in the request's attribute, or <tt>null</tt>
	 *         if not available.
	 */
	protected ServerRepository getRepository() {
		return RequestAtt.getRepository(getRequest());
	}

	/**
	 * Gets the repository connection from the request's attributes, if
	 * available.
	 * 
	 * @return The repository connection stored in the request's attribute, or
	 *         <tt>null</tt> if not available.
	 */
	protected ServerConnection getConnection() {
		return RequestAtt.getConnection(getRequest());
	}
}
