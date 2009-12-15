/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.store;

/**
 * Manages thread-based {@link Session sessions}.
 */
public class SessionManager {

	/**
	 * The registry for created sessions.
	 */
	private static final InheritableThreadLocal<Session> sessions = new InheritableThreadLocal<Session>() {

		@Override
		protected Session initialValue() {
			return new Session();
		}
	};

	/**
	 * Gets the session that is associated with the thread that calls this
	 * method. A new session object will be created if no such session exists
	 * yet.
	 * 
	 * @return The session for the thread that calls this method.
	 */
	public static Session get() {
		return sessions.get();
	}

	/**
	 * Removes the session that is associated with the thread that calls this
	 * method.
	 */
	public static void remove() {
		sessions.remove();
	}
}
