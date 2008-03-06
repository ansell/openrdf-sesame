/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;

/**
 * Evaluates the TupleExpr and ValueExpr with given tripleSource and bindings.
 */
public interface EvaluationStrategy {

	/**
	 * Evaluates the tuple expression against the supplied triple source with
	 * the specified set of variable bindings as input.
	 * 
	 * @param expr
	 *            The Tuple Expression to evaluate
	 * @param bindings
	 *            The variables bindings to use for evaluating the expression,
	 *            if applicable.
	 * @return A closeable iterator over the variable binding sets that match
	 *         the tuple expression.
	 */
	public abstract CloseableIteration<BindingSet, QueryEvaluationException> evaluate(TupleExpr expr,
			BindingSet bindings) throws QueryEvaluationException;

	/**
	 * Gets the value of this expression.
	 * 
	 * @param bindings
	 *            The variables bindings to use for evaluating the expression,
	 *            if applicable.
	 * @return The Value that this expression evaluates to, or <tt>null</tt>
	 *         if the expression could not be evaluated.
	 */
	public abstract Value getValue(ValueExpr expr, BindingSet bindings);

	/**
	 * Evaluates the boolean expression on the supplied TripleSource object.
	 * 
	 * @param bindings
	 *            The variables bindings to use for evaluating the expression,
	 *            if applicable.
	 * @return The result of the evaluation.
	 * @throws BooleanExprEvaluationException
	 *             If the boolean expression could not be evaluated, for example
	 *             when comparing two incompatible operands. When thrown, the
	 *             result of the boolean expression is neither <tt>true</tt>
	 *             nor <tt>false</tt>, but unknown.
	 */
	public abstract boolean isTrue(ValueExpr expr, BindingSet bindings)
			throws BooleanExprEvaluationException;
}
