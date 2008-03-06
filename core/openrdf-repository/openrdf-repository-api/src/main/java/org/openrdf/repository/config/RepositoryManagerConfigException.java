/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

/**
 * Exception indicating a server configuration problem.
 */
public class RepositoryManagerConfigException extends Exception {

	private static final long serialVersionUID = 4248675696950927613L;

	public RepositoryManagerConfigException() {
		super();
	}

	public RepositoryManagerConfigException(String message) {
		super(message);
	}

	public RepositoryManagerConfigException(Throwable t) {
		super(t);
	}

	public RepositoryManagerConfigException(String message, Throwable t) {
		super(message, t);
	}
}
