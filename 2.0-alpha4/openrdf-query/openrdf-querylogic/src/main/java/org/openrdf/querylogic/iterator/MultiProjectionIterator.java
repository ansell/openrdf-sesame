/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylogic.iterator;

import java.util.Iterator;

import org.openrdf.querylogic.TripleSource;
import org.openrdf.querymodel.MultiProjection;
import org.openrdf.queryresult.Solution;
import org.openrdf.util.iterator.CloseableIterator;
import org.openrdf.util.iterator.CloseableIteratorBase;

public class MultiProjectionIterator extends CloseableIteratorBase<Solution> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The wrapped iterator.
	 */
	private Iterator<Solution> _iter;

	private final MultiProjection _projection;

	private Solution _currentBindings;

	private int _nextProjectionIdx;

	public MultiProjectionIterator(CloseableIterator<Solution> eval, MultiProjection projection,
			TripleSource tripleSource, Solution bindings)
	{
		_iter = eval;
		_projection = projection;
	}

	public boolean hasNext() {
		return _currentBindings != null && _nextProjectionIdx < _projection.getProjections().size()
				|| _iter.hasNext();
	}

	public Solution next() {
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
	public void close()
	{
		if (!isClosed()) {
			_nextProjectionIdx = _projection.getProjections().size();
			_currentBindings = null;
		}

		super.close();
	}
}
