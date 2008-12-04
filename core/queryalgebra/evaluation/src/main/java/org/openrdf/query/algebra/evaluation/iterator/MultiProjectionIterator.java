/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.List;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIterationBase;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.ProjectionElemList;

public class MultiProjectionIterator extends CloseableIterationBase<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final List<ProjectionElemList> projections;

	private final CloseableIteration<BindingSet, QueryEvaluationException> iter;

	private final BindingSet parentBindings;

	/*-----------*
	 * Variables *
	 *-----------*/

	private BindingSet currentBindings;

	private volatile int nextProjectionIdx;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MultiProjectionIterator(MultiProjection multiProjection,
			CloseableIteration<BindingSet, QueryEvaluationException> iter, BindingSet bindings)
	{
		this.projections = multiProjection.getProjections();
		this.iter = iter;
		this.parentBindings = bindings;

		// initialize out-of-range to enforce a fetch of the first result upon
		// first use
		nextProjectionIdx = projections.size();
	}

	/*---------*
	 * Methods *
	 *---------*/

	public boolean hasNext()
		throws QueryEvaluationException
	{
		return nextProjectionIdx < projections.size() || iter.hasNext();
	}

	public BindingSet next()
		throws QueryEvaluationException
	{
		int idx = nextProjectionIdx;

		if (idx >= projections.size()) {
			currentBindings = iter.next();
			idx = nextProjectionIdx = 0;
		}

		ProjectionElemList nextProjection = projections.get(idx);
		nextProjectionIdx++;

		return ProjectionIterator.project(nextProjection, currentBindings, parentBindings);
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void handleClose()
		throws QueryEvaluationException
	{
		super.handleClose();
		iter.close();
		nextProjectionIdx = projections.size();
	}
}
