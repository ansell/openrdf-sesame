/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.store;

/**
 * Stores thread-based session information. Sessions are typically initialized
 * in servers.
 */
public class Session {

	private volatile String username;
	
	/**
	 * Retrieve the username of the current user
	 * 
	 * @return the username of the current user
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username of the current user.
	 * 
	 * @param username
	 *        the username of the current user, as a URI.
	 */
	public void setUsername(String username) {
		this.username = username;
	}
}
