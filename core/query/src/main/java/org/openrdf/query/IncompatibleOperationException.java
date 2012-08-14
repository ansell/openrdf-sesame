/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

/**
 * An exception indicating that a string could not be parsed into an operation
 * of the expected type by the parser.
 * 
 * @author jeen
 */
public class IncompatibleOperationException extends MalformedQueryException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4926665776729656410L;

	public IncompatibleOperationException() {
		super();
	}

	public IncompatibleOperationException(String message) {
		super(message);
	}

	public IncompatibleOperationException(Throwable t) {
		super(t);
	}

	public IncompatibleOperationException(String message, Throwable t) {
		super(message, t);
	}
}
