/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol;

import org.openrdf.repository.RepositoryException;

public class UnauthorizedException extends RepositoryException {

	private static final long serialVersionUID = 4322677542795160482L;

	public UnauthorizedException() {
		super();
	}

	public UnauthorizedException(String msg) {
		super(msg);
	}

	public UnauthorizedException(Throwable t) {
		super(t);
	}

	public UnauthorizedException(String msg, Throwable t) {
		super(msg, t);
	}
}
