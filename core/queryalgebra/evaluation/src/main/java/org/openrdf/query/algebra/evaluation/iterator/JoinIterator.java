/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;
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

		// Initialize with empty iteration so that var is not null
		rightIter = new EmptyIteration<BindingSet, QueryEvaluationException>();
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected BindingSet getNextElement()
		throws QueryEvaluationException
	{
		while (rightIter.hasNext() || leftIter.hasNext()) {
			if (rightIter.hasNext()) {
				return rightIter.next();
			}

			// Right iteration exhausted
			rightIter.close();

			if (leftIter.hasNext()) {
				rightIter = strategy.evaluate(join.getRightArg(), leftIter.next());
			}
		}

		return null;
	}

	@Override
	protected void handleClose()
		throws QueryEvaluationException
	{
		super.handleClose();

		leftIter.close();
		rightIter.close();
	}
}
