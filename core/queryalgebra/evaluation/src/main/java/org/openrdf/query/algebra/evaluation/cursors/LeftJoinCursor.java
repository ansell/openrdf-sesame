/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.result.Cursor;
import org.openrdf.store.StoreException;

public class LeftJoinCursor implements Cursor<BindingSet> {

	/*-----------*
	 * Constants *
	 *-----------*/

	private EvaluationStrategy strategy;

	private final LeftJoin join;

	/**
	 * The set of binding names that are "in scope" for the filter. The filter
	 * must not include bindings that are (only) included because of the
	 * depth-first evaluation strategy in the evaluation of the constraint.
	 */
	private final Set<String> scopeBindingNames;

	/*-----------*
	 * Variables *
	 *-----------*/

	private Cursor<BindingSet> leftIter;

	private Cursor<BindingSet> rightIter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public LeftJoinCursor(EvaluationStrategy strategy, LeftJoin join, BindingSet bindings)
		throws StoreException
	{
		this.strategy = strategy;
		this.join = join;
		this.scopeBindingNames = join.getBindingNames();

		leftIter = strategy.evaluate(join.getLeftArg(), bindings);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public BindingSet next()
		throws StoreException
	{
		BindingSet leftBindings = null;
		while (rightIter != null || (leftBindings = leftIter.next()) != null) {

			if (rightIter == null) {
				// Use left arg's bindings in case join fails
				rightIter = strategy.evaluate(join.getRightArg(), leftBindings);
			}

			BindingSet rightBindings;
			while ((rightBindings = rightIter.next()) != null) {

				try {
					if (join.getCondition() == null) {
						return rightBindings;
					}
					else {
						// Limit the bindings to the ones that are in scope for this
						// filter
						QueryBindingSet scopeBindings = new QueryBindingSet(rightBindings);
						scopeBindings.retainAll(scopeBindingNames);

						if (strategy.isTrue(join.getCondition(), scopeBindings)) {
							return rightBindings;
						}
					}
				}
				catch (ValueExprEvaluationException e) {
					// Ignore, condition not evaluated successfully
				}
			}

			rightIter.close();
			rightIter = null;

			if (leftBindings != null) {
				// Join failed, return left arg's bindings
				return leftBindings;
			}
		}

		return null;
	}

	public void close()
		throws StoreException
	{
		if (rightIter != null) {
			rightIter.close();
			rightIter = null;
		}

		leftIter.close();
	}

	@Override
	public String toString() {
		String left = leftIter.toString().replace("\n", "\n\t");
		String right = join.getRightArg().toString();
		if (rightIter != null) {
			right = rightIter.toString();
		}
		ValueExpr condition = join.getCondition();
		String filter = "";
		if (condition != null) {
			filter = condition.toString().trim().replace("\n", "\n\t");
		}
		return "LeftJoin " + filter + "\n\t" + left + "\n\t" + right.replace("\n", "\n\t");
	}
}
