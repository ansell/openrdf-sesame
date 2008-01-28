/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.Set;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;

public class LeftJoinIterator extends LookAheadIteration<BindingSet, QueryEvaluationException> {

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

	private CloseableIteration<BindingSet, QueryEvaluationException> leftIter;

	private CloseableIteration<BindingSet, QueryEvaluationException> rightIter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public LeftJoinIterator(EvaluationStrategy strategy, LeftJoin join, BindingSet bindings)
		throws QueryEvaluationException
	{
		this.strategy = strategy;
		this.join = join;
		this.scopeBindingNames = join.getBindingNames();

		leftIter = strategy.evaluate(join.getLeftArg(), bindings);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected BindingSet getNextElement()
		throws QueryEvaluationException
	{
		while (rightIter != null || leftIter.hasNext()) {
			BindingSet leftBindings = null;

			if (rightIter == null) {
				// Use left arg's bindings in case join fails
				leftBindings = leftIter.next();
				rightIter = strategy.evaluate(join.getRightArg(), leftBindings);
			}

			while (rightIter.hasNext()) {
				BindingSet rightBindings = rightIter.next();

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

	@Override
	protected void handleClose()
		throws QueryEvaluationException
	{
		if (rightIter != null) {
			rightIter.close();
			rightIter = null;
		}

		leftIter.close();

		super.handleClose();
	}
}
