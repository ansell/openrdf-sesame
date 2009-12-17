/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.store;


/**
 * Stores thread-based session information. Sessions are typically initialized
 * in servers.
 */
public class Session {

	private String username; 
	
	/**
	 * Retrieve the username of the current user
	 * @return the username of the current user
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Sets the username of the current user.
	 * @param currentUser the username of the current user, as a URI.
	 */
	public void setUsername(String currentUser) {
		this.username = currentUser;
	}
}
