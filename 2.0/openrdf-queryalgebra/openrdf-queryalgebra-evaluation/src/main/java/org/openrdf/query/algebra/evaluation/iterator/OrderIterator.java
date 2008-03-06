/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIterationBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

/**
 * 
 * @author james
 *
 */
public class OrderIterator extends
		CloseableIterationBase<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private CloseableIteration<BindingSet, QueryEvaluationException> iter;

	private Comparator<BindingSet> comparator;

	private Iterator<BindingSet> ordered;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public OrderIterator(
			CloseableIteration<BindingSet, QueryEvaluationException> iter,
			Comparator<BindingSet> comparator) {
		this.iter = iter;
		this.comparator = comparator;
	}

	/*---------*
	 * Methods *
	 *---------*/

	private Iterator<BindingSet> getOrderedIterator()
			throws QueryEvaluationException {
		if (ordered == null) {
			List<BindingSet> list = new ArrayList<BindingSet>(1024);
			while (iter.hasNext()) {
				list.add(iter.next());
			}
			Collections.sort(list, comparator);
			ordered = list.iterator();
		}
		return ordered;
	}

	public boolean hasNext() throws QueryEvaluationException {
		return getOrderedIterator().hasNext();
	}

	public BindingSet next() throws QueryEvaluationException {
		return getOrderedIterator().next();
	}

	public void remove() throws QueryEvaluationException {
		throw new UnsupportedOperationException();
	}

}
