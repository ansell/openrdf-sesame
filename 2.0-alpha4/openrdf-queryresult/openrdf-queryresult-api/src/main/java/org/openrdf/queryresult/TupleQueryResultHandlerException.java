/*  Copyright (C) 2001-2006 Aduna (http://www.aduna-software.com/)
 *
 *  This software is part of the Sesame Framework. It is licensed under
 *  the following two licenses as alternatives:
 *
 *   1. Open Software License (OSL) v3.0
 *   2. GNU Lesser General Public License (LGPL) v2.1 or any newer
 *      version
 *
 *  By using, modifying or distributing this software you agree to be 
 *  bound by the terms of at least one of the above licenses.
 *
 *  See the file LICENSE.txt that is distributed with this software
 *  for the complete terms and further details.
 */
package org.openrdf.queryresult;

/**
 * An exception that can be thrown by an TupleQueryResultHandler when it encounters an
 * unrecoverable error. If an exception is associated with the error then this
 * exception can be wrapped in an TupleHandlerException and can later be
 * retrieved from it when the TupleHandlerException is catched using the
 * <tt>getCause()</tt>.
 */
public class TupleQueryResultHandlerException extends Exception {

	private static final long serialVersionUID = 8530574857852836665L;

	/**
	 * Creates a new TupleQueryResultHandlerException.
	 *
	 * @param msg An error message.
	 */
	public TupleQueryResultHandlerException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new TupleQueryResultHandlerException.
	 *
	 * @param cause The cause of the exception.
	 */
	public TupleQueryResultHandlerException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new TupleQueryResultHandlerException wrapping another exception.
	 *
	 * @param msg An error message.
	 * @param cause The cause of the exception.
	 */
	public TupleQueryResultHandlerException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
