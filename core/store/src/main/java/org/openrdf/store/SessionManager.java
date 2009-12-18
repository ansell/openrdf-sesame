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
	private static final InheritableThreadLocal<Session> sessions = new InheritableThreadLocal<Session>();

	/**
	 * Gets the session that is associated with the thread that calls this
	 * method. A new session object will be created if no such session exists
	 * yet.
	 * 
	 * @return The session for the thread that calls this method.
	 */
	public static Session getOrCreate() {
		Session session = sessions.get();
		if (session == null) {
			session = new Session();
			sessions.set(session);
		}
		return session;
	}

	/**
	 * Gets the session that is associated with the thread that calls this
	 * method.
	 * 
	 * @return The session for the thread that calls this method, or
	 *         <tt>null</tt> if no session is associted with the calling thread.
	 */
	public static Session get() {
		return sessions.get();
	}

	/**
	 * Sets the session that is associated with the thread that calls this
	 * method.
	 * 
	 * @param session
	 *        The (new) session for the calling thread.
	 */
	public static void set(Session session) {
		sessions.set(session);
	}

	/**
	 * Removes the session that is associated with the thread that calls this
	 * method.
	 */
	public static void remove() {
		sessions.remove();
	}
}
