/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage;

public class MalformedQueryException extends Exception {

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
	
