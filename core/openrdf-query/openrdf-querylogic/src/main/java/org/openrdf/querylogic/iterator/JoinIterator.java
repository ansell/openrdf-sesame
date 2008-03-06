/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylogic.iterator;

import org.openrdf.querylogic.EvaluationStrategy;
import org.openrdf.querylogic.TripleSource;
import org.openrdf.querymodel.Join;
import org.openrdf.queryresult.Solution;
import org.openrdf.util.iterator.CloseableIterator;
import org.openrdf.util.iterator.LookAheadIterator;

public class JoinIterator extends LookAheadIterator<Solution> {

	private EvaluationStrategy _strategy;

	private final Join _join;

	private TripleSource _tripleSource;

	private CloseableIterator<Solution> _leftIter;

	private CloseableIterator<Solution> _rightIter;

	public JoinIterator(EvaluationStrategy strategy, Join join, TripleSource tripleSource, Solution bindings) {
		_strategy = strategy;
		_join = join;
		_tripleSource = tripleSource;
		_leftIter = _strategy.evaluate(_join.getLeftArg(), tripleSource, bindings);
	}

	@Override
	protected Solution getNextElement()
	{
		while (_rightIter != null || _leftIter.hasNext()) {
			if (_rightIter == null) {
				_rightIter = _strategy.evaluate(_join.getRightArg(), _tripleSource, _leftIter.next());
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
