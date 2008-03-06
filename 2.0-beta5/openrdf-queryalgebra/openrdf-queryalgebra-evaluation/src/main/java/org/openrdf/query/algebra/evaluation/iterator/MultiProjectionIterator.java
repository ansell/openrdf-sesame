/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIterationBase;
import info.aduna.iteration.Iteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.MultiProjection;

public class MultiProjectionIterator extends CloseableIterationBase<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The wrapped iterator.
	 */
	private Iteration<BindingSet, QueryEvaluationException> _iter;

	private final MultiProjection _projection;

	private BindingSet _currentBindings;

	private int _nextProjectionIdx;

	public MultiProjectionIterator(CloseableIteration<BindingSet, QueryEvaluationException> iter,
			MultiProjection projection, BindingSet bindings)
	{
		_iter = iter;
		_projection = projection;
	}

	public boolean hasNext()
		throws QueryEvaluationException
	{
		return _currentBindings != null && _nextProjectionIdx < _projection.getProjections().size()
				|| _iter.hasNext();
	}

	public BindingSet next()
		throws QueryEvaluationException
	{
		if (_currentBindings == null || _nextProjectionIdx >= _projection.getProjections().size()) {
			_currentBindings = _iter.next();
			_nextProjectionIdx = 0;
		}

		return ProjectionIterator.project(_projection.getProjections().get(_nextProjectionIdx++),
				_currentBindings);
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void handleClose()
		throws QueryEvaluationException
	{
		_nextProjectionIdx = _projection.getProjections().size();
		_currentBindings = null;
		super.handleClose();
	}
}
