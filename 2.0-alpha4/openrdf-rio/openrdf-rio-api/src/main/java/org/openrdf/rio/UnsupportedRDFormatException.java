/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

/**
 * An exception indicating that a specific RDF format is not supported.
 */
public class UnsupportedRDFormatException extends Exception {

	private static final long serialVersionUID = -2709196386078518696L;

	/**
	 * Creates a new UnsupportedRDFormatException.
	 * 
	 * @param msg
	 *        An error message.
	 */
	public UnsupportedRDFormatException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new UnsupportedRDFormatException.
	 * 
	 * @param cause
	 *        The cause of the exception.
	 */
	public UnsupportedRDFormatException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new UnsupportedRDFormatException wrapping another exception.
	 * 
	 * @param msg
	 *        An error message.
	 * @param cause
	 *        The cause of the exception.
	 */
	public UnsupportedRDFormatException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
