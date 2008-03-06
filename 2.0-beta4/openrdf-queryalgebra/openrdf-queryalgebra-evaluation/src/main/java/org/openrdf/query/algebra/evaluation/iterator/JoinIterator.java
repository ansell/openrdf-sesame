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

	private EvaluationStrategy _strategy;

	private final Join _join;

	private CloseableIteration<BindingSet, QueryEvaluationException> _leftIter;

	private CloseableIteration<BindingSet, QueryEvaluationException> _rightIter;

	public JoinIterator(EvaluationStrategy strategy, Join join, BindingSet bindings)
		throws QueryEvaluationException
	{
		_strategy = strategy;
		_join = join;
		_leftIter = _strategy.evaluate(_join.getLeftArg(), bindings);
	}

	@Override
	protected BindingSet getNextElement()
		throws QueryEvaluationException
	{
		while (_rightIter != null || _leftIter.hasNext()) {
			if (_rightIter == null) {
				_rightIter = _strategy.evaluate(_join.getRightArg(), _leftIter.next());
			}

			if (_rightIter.hasNext()) {
				return _rightIter.next();
			}
			else {
				_rightIter.close();
				_rightIter = null;
			}
		}

		return null;
	}

	@Override
	protected void handleClose()
		throws QueryEvaluationException
	{
		if (_rightIter != null) {
			_rightIter.close();
			_rightIter = null;
		}

		_leftIter.close();

		super.handleClose();
	}
}
