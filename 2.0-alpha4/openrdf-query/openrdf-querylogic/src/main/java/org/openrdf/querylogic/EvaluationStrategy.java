/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylogic;

import org.openrdf.model.Value;
import org.openrdf.querymodel.BooleanExpr;
import org.openrdf.querymodel.TupleExpr;
import org.openrdf.querymodel.ValueExpr;
import org.openrdf.queryresult.Solution;
import org.openrdf.util.iterator.CloseableIterator;

/**
 * Evaluates the TupleExpr and ValueExpr with given tripleSource and bindings.
 */
public interface EvaluationStrategy {

	/**
	 * Evaluates the tuple expression against the supplied triple source with the
	 * specified set of variable bindings as input.
	 * 
	 * @param expr
	 * 		The Tuple Expression to evaluate
	 * @param tripleSource
	 *        The triple source to use for evaluating the expressions, if
	 *        applicable.
	 * @param bindings
	 *        The variables bindings to use for evaluating the expression, if
	 *        applicable.
	 * @return A closeable iterator over the variable binding sets that match the
	 *         tuple expression.
	 */
	public abstract CloseableIterator<Solution> evaluate(TupleExpr expr, TripleSource tripleSource,
			Solution bindings);

	/**
	 * Gets the value of this expression.
	 * 
	 * @param tripleSource
	 *        The triple source to use for evaluating the expressions, if
	 *        applicable.
	 * @param bindings
	 *        The variables bindings to use for evaluating the expression, if
	 *        applicable.
	 * @return The Value that this expression evaluates to, or <tt>null</tt> if
	 *         the expression could not be evaluated.
	 */
	public abstract Value getValue(ValueExpr expr, TripleSource tripleSource, Solution bindings);

	/**
	 * Evaluates the boolean expression on the supplied TripleSource object.
	 * 
	 * @param tripleSource
	 *        The triple source to use for evaluating the expressions, if
	 *        applicable.
	 * @param bindings
	 *        The variables bindings to use for evaluating the expression, if
	 *        applicable.
	 * @return The result of the evaluation.
	 * @throws BooleanExprEvaluationException
	 *         If the boolean expression could not be evaluated, for example when
	 *         comparing two incompatible operands. When thrown, the result of
	 *         the boolean expression is neither <tt>true</tt> nor
	 *         <tt>false</tt>, but unknown.
	 */
	public abstract boolean isTrue(BooleanExpr expr, TripleSource tripleSource, Solution bindings)
		throws BooleanExprEvaluationException;

}
