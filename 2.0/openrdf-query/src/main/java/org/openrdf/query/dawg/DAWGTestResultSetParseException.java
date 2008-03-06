/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.dawg;

import org.openrdf.OpenRDFException;

/**
 * An exception that is thrown to indicate that the parsing of a DAWG Test
 * Result Set graph failed due to an incompatible or incomplete graph.
 */
public class DAWGTestResultSetParseException extends OpenRDFException {

	private static final long serialVersionUID = -8655777672973690037L;

	/**
	 * Creates a new DAWGTestResultSetParseException.
	 * 
	 * @param msg
	 *        An error message.
	 */
	public DAWGTestResultSetParseException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new DAWGTestResultSetParseException wrapping another exception.
	 * 
	 * @param cause
	 *        The cause of the exception.
	 */
	public DAWGTestResultSetParseException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new DAWGTestResultSetParseException wrapping another exception.
	 * 
	 * @param msg
	 *        An error message.
	 * @param cause
	 *        The cause of the exception.
	 */
	public DAWGTestResultSetParseException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
