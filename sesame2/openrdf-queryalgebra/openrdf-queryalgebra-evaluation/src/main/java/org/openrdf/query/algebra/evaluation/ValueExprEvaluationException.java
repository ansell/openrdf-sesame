/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.ValueExpr;

/**
 * An exception indicating that a {@link ValueExpr} could not be evaluated due
 * to illegal or incompatible values. When thrown, the result of the evaluation
 * should be considered to be "unknown".
 * 
 * @author Arjohn Kampman
 */
public class ValueExprEvaluationException extends QueryEvaluationException {

	private static final long serialVersionUID = -3633440570594631529L;

	public ValueExprEvaluationException() {
		super();
	}

	public ValueExprEvaluationException(String message) {
		super(message);
	}

	public ValueExprEvaluationException(String message, Throwable t) {
		super(message, t);
	}

	public ValueExprEvaluationException(Throwable t) {
		super(t);
	}
}
