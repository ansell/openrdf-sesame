/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import org.openrdf.OpenRDFException;

/**
 * An exception thrown by classes from the Repository API to indicate an error.
 * Most of the time, this exception will wrap another exception that indicates
 * the actual source of the error.
 */
public class RepositoryException extends OpenRDFException {

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
