/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

/**
 * An exception indicating that the evaluation of a query has been interrupted,
 * for example because it took too long to complete.
 * 
 * @author Arjohn Kampman
 */
public class QueryInterruptedException extends EvaluationException {

	private static final long serialVersionUID = -1261311645990563247L;

	public QueryInterruptedException() {
		super();
	}

	public QueryInterruptedException(String message) {
		super(message);
	}

	public QueryInterruptedException(String message, Throwable t) {
		super(message, t);
	}

	public QueryInterruptedException(Throwable t) {
		super(t);
	}
}
