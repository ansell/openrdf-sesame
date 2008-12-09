/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http.exceptions;

import org.openrdf.store.StoreException;


/**
 *
 * @author James Leigh
 */
public class IllegalStatementException extends StoreException {

	private static final long serialVersionUID = 2864140910168652446L;

	public IllegalStatementException() {
		super();
	}

	public IllegalStatementException(String msg, Throwable t) {
		super(msg, t);
	}

	public IllegalStatementException(String msg) {
		super(msg);
	}

	public IllegalStatementException(Throwable t) {
		super(t);
	}

}
