/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

import org.openrdf.sail.config.SailConfigException;

/**
 * Exception indicating a repository configuration problem.
 * 
 * @author Arjohn Kampman
 */
public abstract class RepositoryConfigException extends SailConfigException {

	private static final long serialVersionUID = -6643040675968955429L;

	public RepositoryConfigException() {
		super();
	}

	public RepositoryConfigException(String message) {
		super(message);
	}

	public RepositoryConfigException(Throwable t) {
		super(t);
	}

	public RepositoryConfigException(String message, Throwable t) {
		super(message, t);
	}
}
