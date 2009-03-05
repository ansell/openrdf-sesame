/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

import org.openrdf.OpenRDFException;

/**
 * An exception that can be thrown by an RDFHandler when it encounters an
 * unrecoverable error. If an exception is associated with the error then this
 * exception can be wrapped in an RDFHandlerException and can later be retrieved
 * from it when the RDFHandlerException is catched using the <tt>getCause()</tt>
 * .
 */
public class RDFHandlerException extends OpenRDFException {

	private static final long serialVersionUID = -1931215293637533642L;

	/**
	 * Creates a new RDFHandlerException.
	 * 
	 * @param msg
	 *        An error message.
	 */
	public RDFHandlerException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new RDFHandlerException.
	 * 
	 * @param cause
	 *        The cause of the exception.
	 */
	public RDFHandlerException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new RDFHandlerException wrapping another exception.
	 * 
	 * @param msg
	 *        An error message.
	 * @param cause
	 *        The cause of the exception.
	 */
	public RDFHandlerException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
