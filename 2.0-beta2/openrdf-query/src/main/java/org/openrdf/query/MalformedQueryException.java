/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.OpenRDFException;

/**
 * An exception indicating that a query could not be processed by the query
 * parser, typically due to syntax errors.
 * 
 * @author jeen
 */
public class MalformedQueryException extends OpenRDFException {

	private static final long serialVersionUID = 1210214405486786142L;

	public MalformedQueryException() {
		super();
	}

	public MalformedQueryException(String message) {
		super(message);
	}

	public MalformedQueryException(Throwable t) {
		super(t);
	}

	public MalformedQueryException(String message, Throwable t) {
		super(message, t);
	}
}
