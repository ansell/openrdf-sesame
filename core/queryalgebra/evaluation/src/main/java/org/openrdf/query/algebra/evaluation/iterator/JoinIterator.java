/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;

public class JoinIterator extends LookAheadIteration<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final EvaluationStrategy strategy;

	private final Join join;

	/*-----------*
	 * Variables *
	 *-----------*/

	private CloseableIteration<BindingSet, QueryEvaluationException> leftIter;

	private CloseableIteration<BindingSet, QueryEvaluationException> rightIter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public JoinIterator(EvaluationStrategy strategy, Join join, BindingSet bindings)
		throws QueryEvaluationException
	{
		this.strategy = strategy;
		this.join = join;

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
			if (rightIter == null) {
				rightIter = strategy.evaluate(join.getRightArg(), leftIter.next());
			}

			if (rightIter.hasNext()) {
				return rightIter.next();
			}
			else {
				rightIter.close();
				rightIter = null;
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
