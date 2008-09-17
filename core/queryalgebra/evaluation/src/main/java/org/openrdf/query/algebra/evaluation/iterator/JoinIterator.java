/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.StoreException;
import org.openrdf.query.BindingSet;
import org.openrdf.query.EvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;

public class JoinIterator extends LookAheadIteration<BindingSet, StoreException> {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final EvaluationStrategy strategy;

	private final TupleExpr rightArg;

	/*-----------*
	 * Variables *
	 *-----------*/

	private CloseableIteration<BindingSet, StoreException> leftIter;

	private CloseableIteration<BindingSet, StoreException> rightIter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public JoinIterator(EvaluationStrategy strategy, CloseableIteration<BindingSet, StoreException> leftIter,
			TupleExpr rightArg, BindingSet bindings)
		throws EvaluationException
	{
		this.strategy = strategy;
		this.leftIter = leftIter;
		this.rightArg = rightArg;

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
			if (rightIter.hasNext()) {
				return rightIter.next();
			}

			// Right iteration exhausted
			rightIter.close();

			if (leftIter.hasNext()) {
				rightIter = strategy.evaluate(rightArg, leftIter.next());
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
