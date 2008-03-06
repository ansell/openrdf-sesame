/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation;

/**
 * An exception that can be thrown by BooleanExpr.isTrue(TripleSource) to
 * indicate that the expression could not be evaluated to <tt>true</tt> or
 * <tt>false</tt>. When thrown, the result of the method should be considered to
 * be "unknown".
 */
public class BooleanExprEvaluationException extends RuntimeException {

	private static final long serialVersionUID = -3633440570594631529L;

	public BooleanExprEvaluationException() {
		super();
	}

	public BooleanExprEvaluationException(String message) {
		super(message);
	}

	public BooleanExprEvaluationException(String message, Throwable t) {
		super(message, t);
	}

	public BooleanExprEvaluationException(Throwable t) {
		super(t);
	}
}
