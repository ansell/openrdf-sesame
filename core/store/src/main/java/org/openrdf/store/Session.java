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

	private URI activeRole;
	private URI currentUser; 
	
	/**
	 * Retrieve the active role of the user
	 * @return the uri of the current user's active role
	 */
	public URI getActiveRole() {
		return activeRole;
	}
	
	/**
	 * Sets the user's active role.
	 * @param activeRole a uri denoting the active role of the user.
	 */
	public void setActiveRole(URI activeRole) {
		this.activeRole = activeRole;
	}
	
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
