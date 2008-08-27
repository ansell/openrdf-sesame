/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf;

import org.openrdf.repository.RepositoryException;

/**
 * An exception thrown classes from the Repository or SAIL API to indicate an
 * error. Most of the time, this exception will wrap another exception that
 * indicates the actual source of the error.
 */
public class StoreException extends RepositoryException {

	private static final long serialVersionUID = -3216054915937011603L;

	public StoreException() {
		super();
	}

	public StoreException(String msg) {
		super(msg);
	}

	public StoreException(Throwable t) {
		super(t);
	}

	public StoreException(String msg, Throwable t) {
		super(msg, t);
	}
}
