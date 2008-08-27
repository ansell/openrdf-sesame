/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import org.openrdf.StoreException;

/**
 * Indicates that the current write operation did not succeed because the SAIL
 * cannot be written to, it can only be read from.
 * 
 * @author James Leigh
 */
public class RepositoryReadOnlyException extends StoreException {
	private static final long serialVersionUID = 750575278848692139L;

	public RepositoryReadOnlyException() {
		super();
	}

	public RepositoryReadOnlyException(String msg, Throwable t) {
		super(msg, t);
	}

	public RepositoryReadOnlyException(String msg) {
		super(msg);
	}

	public RepositoryReadOnlyException(Throwable t) {
		super(t);
	}

}
