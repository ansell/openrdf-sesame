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

	private static ThreadLocal<URI> activeRole = new ThreadLocal<URI>();
	private static ThreadLocal<URI> currentUser = new ThreadLocal<URI>(); 
	
	/**
	 * Retrieve the active role of the user
	 * @return the uri of the current user's active role
	 */
	public static URI getActiveRole() {
		return activeRole.get();
	}
	
	/**
	 * Sets the user's active role.
	 * @param activeRole a uri denoting the active role of the user.
	 */
	public static void setActiveRole(URI activeRole) {
		Session.activeRole.set(activeRole);
	}
	
	/**
	 * Retrieve the id of the current user
	 * @return the id of the current user, as a URI
	 */
	public static URI getCurrentUser() {
		return currentUser.get();
	}
	
	/**
	 * Sets the id of the current user.
	 * @param currentUser the id of the current user, as a URI.
	 */
	public static void setCurrentUser(URI currentUser) {
		Session.currentUser.set(currentUser);
	}
}
