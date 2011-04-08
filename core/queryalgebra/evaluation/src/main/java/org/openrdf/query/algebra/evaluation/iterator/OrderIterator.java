/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.TreeMap;

import info.aduna.collections.iterators.EmptyIterator;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.DelayedIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

/**
 * Sorts the input and optionally applies limit and distinct.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public class OrderIterator extends DelayedIteration<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final CloseableIteration<BindingSet, QueryEvaluationException> iter;

	private final Comparator<BindingSet> comparator;

	private final int limit;

	private final boolean distinct;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public OrderIterator(CloseableIteration<BindingSet, QueryEvaluationException> iter,
			Comparator<BindingSet> comparator)
	{
		this(iter, comparator, Integer.MAX_VALUE, false);
	}

	public OrderIterator(CloseableIteration<BindingSet, QueryEvaluationException> iter,
			Comparator<BindingSet> comparator, int limit, boolean distinct)
	{
		this.iter = iter;
		this.comparator = comparator;
		this.limit = limit;
		this.distinct = distinct;
	}

	/*---------*
	 * Methods *
	 *---------*/

	protected Iteration<BindingSet, QueryEvaluationException> createIteration()
		throws QueryEvaluationException
	{
		TreeMap<BindingSet, Collection<BindingSet>> map;
		map = new TreeMap<BindingSet, Collection<BindingSet>>(comparator);

		int size = 0;

		try {
			while (iter.hasNext()) {
				BindingSet next = iter.next();

				// Add this binding set if the limit hasn't been reached yet, or if
				// it is sorted before the current lowest value
				if (size < limit || comparator.compare(next, map.lastKey()) < 0) {
					Collection<BindingSet> list = map.get(next);
					if (list == null) {
						list = distinct ? new LinkedHashSet<BindingSet>() : new LinkedList<BindingSet>();
						map.put(next, list);
					}

					if (list.add(next)) {
						size++;
					}

					if (size > limit) {
						// Discard binding set that is currently sorted last
						BindingSet lastKey = map.lastKey();
						Collection<BindingSet> lastResults = map.get(lastKey);

						assert !lastResults.isEmpty();

						if (lastResults instanceof LinkedList<?>) {
							((LinkedList<BindingSet>)lastResults).removeLast();
						}
						else {
							Iterator<BindingSet> iter = lastResults.iterator();
							while (iter.hasNext()) {
								iter.next();
							}
							iter.remove();
						}

						size--;

						if (lastResults.isEmpty()) {
							map.remove(lastKey);
						}
					}
				}
			}
		}
		finally {
			iter.close();
		}

		final Iterator<Collection<BindingSet>> values = map.values().iterator();

		return new LookAheadIteration<BindingSet, QueryEvaluationException>() {

			// Initialize with empty iteration so that var is never null
			private volatile Iterator<BindingSet> iterator = new EmptyIterator<BindingSet>();

			protected BindingSet getNextElement() {
				while (!iterator.hasNext() && values.hasNext()) {
					iterator = values.next().iterator();
				}
				if (iterator.hasNext()) {
					return iterator.next();
				}
				return null;
			}
		};
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
		iter.close();
		super.handleClose();
	}
}
