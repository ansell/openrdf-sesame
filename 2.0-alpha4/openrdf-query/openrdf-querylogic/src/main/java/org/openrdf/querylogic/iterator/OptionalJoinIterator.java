/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylogic.iterator;

import org.openrdf.querylogic.BooleanExprEvaluationException;
import org.openrdf.querylogic.EvaluationStrategy;
import org.openrdf.querylogic.TripleSource;
import org.openrdf.querymodel.OptionalJoin;
import org.openrdf.queryresult.Solution;
import org.openrdf.util.iterator.CloseableIterator;
import org.openrdf.util.iterator.LookAheadIterator;

public class OptionalJoinIterator extends LookAheadIterator<Solution> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private EvaluationStrategy _strategy;

	private final OptionalJoin _join;

	private TripleSource _tripleSource;

	private CloseableIterator<Solution> _leftIter;

	private CloseableIterator<Solution> _rightIter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public OptionalJoinIterator(EvaluationStrategy strategy, OptionalJoin join, TripleSource tripleSource, Solution bindings) {
		_strategy = strategy;
		_join = join;
		_tripleSource = tripleSource;
		_leftIter = _strategy.evaluate(_join.getLeftArg(), tripleSource, bindings);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected Solution getNextElement()
	{
		while (_rightIter != null || _leftIter.hasNext()) {
			Solution leftBindings = null;

			if (_rightIter == null) {
				// Use left arg's bindings in case join fails
				leftBindings = _leftIter.next();
				_rightIter = _strategy.evaluate(_join.getRightArg(), _tripleSource, leftBindings);
			}

			while (_rightIter.hasNext()) {
				Solution rightBindings = _rightIter.next();

				try {
					if (_join.getCondition() == null || _strategy.isTrue(_join.getCondition(), _tripleSource, rightBindings)) {
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
	public void close()
	{
		if (_rightIter != null) {
			_rightIter.close();
			_rightIter = null;
		}

		_leftIter.close();

		super.close();
	}
}
