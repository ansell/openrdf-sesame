/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import org.openrdf.sail.SailException;

/**
 * An exception thrown classes from the Repository API to indicate an error.
 * Most of the time, this exception will wrap another exception that indicates
 * the actual source of the error.
 */
@Deprecated
public class RepositoryException extends SailException {

	private static final long serialVersionUID = -5345676977796873420L;

	public RepositoryException() {
		super();
	}

	public RepositoryException(String msg) {
		super(msg);
	}

	public RepositoryException(Throwable t) {
		super(t);
	}

	public RepositoryException(String msg, Throwable t) {
		super(msg, t);
	}
}
