/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIterationBase;

import org.openrdf.StoreException;
import org.openrdf.query.BindingSet;
import org.openrdf.query.EvaluationException;

/**
 * 
 * @author james
 *
 */
public class OrderIterator extends
		CloseableIterationBase<BindingSet, StoreException> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private CloseableIteration<BindingSet, StoreException> iter;

	private Comparator<BindingSet> comparator;

	private Iterator<BindingSet> ordered;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public OrderIterator(
			CloseableIteration<BindingSet, StoreException> iter,
			Comparator<BindingSet> comparator) {
		this.iter = iter;
		this.comparator = comparator;
	}

	/*---------*
	 * Methods *
	 *---------*/

	private Iterator<BindingSet> getOrderedIterator()
			throws StoreException {
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

	public boolean hasNext() throws StoreException {
		return getOrderedIterator().hasNext();
	}

	public BindingSet next() throws StoreException {
		return getOrderedIterator().next();
	}

	public void remove() throws EvaluationException {
		throw new UnsupportedOperationException();
	}

}
