/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.store;

/**
 * Exception indicating that a method call on a connection failed because the
 * connection has already been closed.
 * 
 * @author Arjohn Kampman
 */
public class ConnectionClosedException extends StoreException {

	private static final long serialVersionUID = 4461619201320450054L;

	public ConnectionClosedException() {
		super("Connection has been closed");
	}

	public ConnectionClosedException(String msg) {
		super(msg);
	}

	public ConnectionClosedException(Throwable t) {
		this("Connection has been closed", t);
	}

	public ConnectionClosedException(String msg, Throwable t) {
		super(msg, t);
	}
}
