/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.OpenRDFException;

/**
 * An exception indicating that the evaluation of a query failed.
 * 
 * @author Arjohn Kampman
 */
public class QueryEvaluationException extends OpenRDFException {

	private static final long serialVersionUID = 602749602257031631L;

	public QueryEvaluationException() {
		super();
	}

	/**
	 * Creates a new TupleQueryResultHandlerException.
	 * 
	 * @param msg
	 *        An error message.
	 */
	public QueryEvaluationException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new TupleQueryResultHandlerException wrapping another exception.
	 * 
	 * @param cause
	 *        The cause of the exception.
	 */
	public QueryEvaluationException(Throwable cause) {
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
	public QueryEvaluationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
