/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.OpenRDFException;

/**
 * An exception that can be thrown by an TupleQueryResultHandler when it
 * encounters an unrecoverable error. If an exception is associated with the
 * error then this exception can be wrapped in a TupleHandlerException and can
 * later be retrieved from it when the TupleHandlerException is caught using the
 * <tt>getCause()</tt>.
 */
public class TupleQueryResultHandlerException extends OpenRDFException {

	private static final long serialVersionUID = 8530574857852836665L;

	/**
	 * Creates a new TupleQueryResultHandlerException.
	 * 
	 * @param msg
	 *        An error message.
	 */
	public TupleQueryResultHandlerException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new TupleQueryResultHandlerException wrapping another exception.
	 * 
	 * @param cause
	 *        The cause of the exception.
	 */
	public TupleQueryResultHandlerException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new TupleQueryResultHandlerException wrapping another exception.
	 * 
	 * @param msg
	 *        An error message.
	 * @param cause
	 *        The cause of the exception.
	 */
	public TupleQueryResultHandlerException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
