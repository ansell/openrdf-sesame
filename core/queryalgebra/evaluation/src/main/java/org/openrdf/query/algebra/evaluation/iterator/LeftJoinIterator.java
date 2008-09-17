/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.Set;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.StoreException;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

public class LeftJoinIterator extends LookAheadIteration<BindingSet, StoreException> {

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

	private CloseableIteration<BindingSet, StoreException> leftIter;

	private CloseableIteration<BindingSet, StoreException> rightIter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public LeftJoinIterator(EvaluationStrategy strategy, LeftJoin join, BindingSet bindings)
		throws StoreException
	{
		this.strategy = strategy;
		this.join = join;
		this.scopeBindingNames = join.getBindingNames();

		leftIter = strategy.evaluate(join.getLeftArg(), bindings);

		// Initialize with empty iteration so that var is not null
		rightIter = new EmptyIteration<BindingSet, StoreException>();
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected BindingSet getNextElement()
		throws StoreException
	{
		while (rightIter.hasNext() || leftIter.hasNext()) {
			BindingSet leftBindings = null;

			if (!rightIter.hasNext()) {
				// Use left arg's bindings in case join fails
				leftBindings = leftIter.next();

				rightIter.close();
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

			if (leftBindings != null) {
				// Join failed, return left arg's bindings
				return leftBindings;
			}
		}

		return null;
	}

	@Override
	protected void handleClose()
		throws StoreException
	{
		super.handleClose();

		leftIter.close();
		rightIter.close();
	}
}
