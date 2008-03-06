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
import org.openrdf.query.algebra.OptionalJoin;
import org.openrdf.query.algebra.evaluation.BooleanExprEvaluationException;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;

public class OptionalJoinIterator extends LookAheadIteration<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private EvaluationStrategy _strategy;

	private final OptionalJoin _join;

	private CloseableIteration<BindingSet, QueryEvaluationException> _leftIter;

	private CloseableIteration<BindingSet, QueryEvaluationException> _rightIter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public OptionalJoinIterator(EvaluationStrategy strategy, OptionalJoin join, BindingSet bindings) throws QueryEvaluationException {
		_strategy = strategy;
		_join = join;
		_leftIter = _strategy.evaluate(_join.getLeftArg(), bindings);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected BindingSet getNextElement()
		throws QueryEvaluationException
	{
		while (_rightIter != null || _leftIter.hasNext()) {
			BindingSet leftBindings = null;

			if (_rightIter == null) {
				// Use left arg's bindings in case join fails
				leftBindings = _leftIter.next();
				_rightIter = _strategy.evaluate(_join.getRightArg(), leftBindings);
			}

			while (_rightIter.hasNext()) {
				BindingSet rightBindings = _rightIter.next();

				try {
					if (_join.getCondition() == null || _strategy.isTrue(_join.getCondition(), rightBindings)) {
						return rightBindings;
					}
				}
				catch (BooleanExprEvaluationException e) {
					// Ignore, condition not evaluated successfully
				}
			}

			_rightIter.close();
			_rightIter = null;

			if (leftBindings != null) {
				// Join failed, return left arg's bindings
				return leftBindings;
			}
		}

		return null;
	}

	@Override
	public void close() throws QueryEvaluationException
	{
		if (_rightIter != null) {
			_rightIter.close();
			_rightIter = null;
		}

		_leftIter.close();

		super.close();
	}
}
