/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009-2010.
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

	private volatile String username;
	
	private volatile URI userId;
	
	/**
	 * Retrieve the username of the current user
	 * 
	 * @return the username of the current user
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username of the current user. As a side-effect the user ID is reset.
	 * 
	 * @param username
	 *        the username of the current user.
	 */
	public void setUsername(String username) {
		this.username = username;
		setUserId(null);
	}

	/**
	 * Sets the User ID of the current user. For internal use only.
	 * 
	 * @param userId
	 */
	public void setUserId(URI userId) {
		this.userId = userId;
	}

	/**
	 * Retrieve the User ID of the current user. 
	 * 
	 * @return a URI representing the user ID of the current user, or null.
	 */
	public URI getUserId() {
		return userId;
	}
	
}
