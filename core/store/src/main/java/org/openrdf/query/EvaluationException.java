/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.store.StoreException;

/**
 * An exception indicating that the evaluation of a query failed.
 * 
 * @author Arjohn Kampman
 */
public class EvaluationException extends StoreException {

	private static final long serialVersionUID = 602749602257031631L;

	public EvaluationException() {
		super();
	}

	/**
	 * Creates a new TupleQueryResultHandlerException.
	 * 
	 * @param msg
	 *        An error message.
	 */
	public EvaluationException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new TupleQueryResultHandlerException wrapping another exception.
	 * 
	 * @param cause
	 *        The cause of the exception.
	 */
	public EvaluationException(Throwable cause) {
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
	public EvaluationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
