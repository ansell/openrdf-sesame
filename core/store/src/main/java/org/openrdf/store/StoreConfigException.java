/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.store;

import org.openrdf.repository.config.RepositoryConfigException;

/**
 * Exception indicating a store configuration problem.
 * 
 * @author Arjohn Kampman
 */
public class StoreConfigException extends RepositoryConfigException {

	private static final long serialVersionUID = -6643040675968955429L;

	public StoreConfigException() {
		super();
	}

	public StoreConfigException(String message) {
		super(message);
	}

	public StoreConfigException(Throwable t) {
		super(t);
	}

	public StoreConfigException(String message, Throwable t) {
		super(message, t);
	}
}
