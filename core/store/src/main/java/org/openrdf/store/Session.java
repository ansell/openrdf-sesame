/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.store;

import org.openrdf.model.URI;

/**
 * Stores thread-based session information. Sessions are typically initialized
 * in servers.
 */
public class Session {

	private URI currentUser; 
	
	/**
	 * Retrieve the id of the current user
	 * @return the id of the current user, as a URI
	 */
	public URI getCurrentUser() {
		return currentUser;
	}
	
	/**
	 * Sets the id of the current user.
	 * @param currentUser the id of the current user, as a URI.
	 */
	public void setCurrentUser(URI currentUser) {
		this.currentUser = currentUser;
	}
}
