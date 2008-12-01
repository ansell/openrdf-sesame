/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.DelayedIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;
import info.aduna.iteration.IteratorIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

/**
 * @author james
 * @author Arjohn Kampman
 */
public class OrderIterator extends DelayedIteration<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final CloseableIteration<BindingSet, QueryEvaluationException> iter;

	private final Comparator<BindingSet> comparator;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public OrderIterator(CloseableIteration<BindingSet, QueryEvaluationException> iter,
			Comparator<BindingSet> comparator)
	{
		this.iter = iter;
		this.comparator = comparator;
	}

	/*---------*
	 * Methods *
	 *---------*/

	protected Iteration<BindingSet, QueryEvaluationException> createIteration()
		throws QueryEvaluationException
	{
		List<BindingSet> list = Iterations.addAll(iter, new ArrayList<BindingSet>(1024));
		
		if (!isClosed()) {
			Collections.sort(list, comparator);
			return new IteratorIteration<BindingSet, QueryEvaluationException>(list.iterator());
		}
		else {
			return new EmptyIteration<BindingSet, QueryEvaluationException>();
		}
	}

	@Override
	public void remove()
		throws QueryEvaluationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected void handleClose()
		throws QueryEvaluationException
	{
		super.handleClose();
		iter.close();
	}
}
