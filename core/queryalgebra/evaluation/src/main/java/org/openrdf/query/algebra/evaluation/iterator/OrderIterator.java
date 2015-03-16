/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;

import org.mapdb.DB;
import org.mapdb.DB.BTreeMapMaker;
import org.mapdb.DBMaker;
import org.mapdb.Fun;

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

	private final long limit;

	private final boolean distinct;

	private final File tempFile;

	private final DB db;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public OrderIterator(CloseableIteration<BindingSet, QueryEvaluationException> iter,
			Comparator<BindingSet> comparator)
	{
		this(iter, comparator, Integer.MAX_VALUE, false);
	}

	public OrderIterator(CloseableIteration<BindingSet, QueryEvaluationException> iter,
			Comparator<BindingSet> comparator, long limit, boolean distinct)
	{
		this.iter = iter;
		this.comparator = comparator;
		this.limit = limit;
		this.distinct = distinct;

		try {
			this.tempFile = File.createTempFile("order-eval", null);
		}
		catch (IOException e) {
			throw new IOError(e);
		}
		this.db = DBMaker.newFileDB(tempFile).deleteFilesAfterClose().closeOnJvmShutdown().make();
	}

	/*---------*
	 * Methods *
	 *---------*/

	protected Iteration<BindingSet, QueryEvaluationException> createIteration()
		throws QueryEvaluationException
	{
		// NavigableMap<BindingSet, Collection<BindingSet>> map =
		// makeOrderedMap(comparator);

		final NavigableMap<BindingSet, Integer> map = db.createTreeMap("iteration").comparator(comparator).makeOrGet();

		int size = 0;

		try {
			while (iter.hasNext()) {
				BindingSet next = iter.next();

				// Add this binding set if the limit hasn't been reached yet, or if
				// it is sorted before the current lowest value
				if (size < limit || comparator.compare(next, map.lastKey()) < 0) {

					// if (list.iterator().hasNext()) {
					// list = distinct ? makeOrderedSet() : makeList();
					// put(map, next, list);
					// }

					Integer count = map.get(next);

					if (count == null) {
						map.put(next, 1);
					}
					else if (!distinct) {
						map.put(next, ++count);
						size++;
					}

					if (size > limit) {
						// Discard binding set that is currently sorted last
						BindingSet lastKey = map.lastKey();

						Integer lastCount = map.get(lastKey);
						if (lastCount > 1) {
							map.put(lastKey, --lastCount);
						}
						else {
							map.remove(lastKey);
						}
						size--;
					}
				}
			}
		}
		finally {
			iter.close();
		}

		return new LookAheadIteration<BindingSet, QueryEvaluationException>() {

			private volatile Iterator<BindingSet> iterator = map.keySet().iterator();

			private volatile BindingSet currentBindingSet = null;

			private volatile int count = 0;

			protected BindingSet getNextElement() {

				if (count == 0 && iterator.hasNext()) {
					currentBindingSet = iterator.next();
					count = map.get(currentBindingSet);
				}

				if (count > 0) {
					count--;
					return currentBindingSet;
				}

				return null;
			}
		};
	}

	protected List<BindingSet> makeList() {
		return new LinkedList<BindingSet>();
	}

	/**
	 * This is used when distinct is set too true.
	 * 
	 * @return a new Set may be store specific.
	 */
	protected Set<BindingSet> makeOrderedSet() {
		return new LinkedHashSet<BindingSet>();
	}

	protected void removeLast(Collection<BindingSet> lastResults) {
		if (lastResults instanceof LinkedList<?>) {
			((LinkedList<BindingSet>)lastResults).removeLast();
		}
		else if (lastResults instanceof List<?>) {
			((List<BindingSet>)lastResults).remove(lastResults.size() - 1);
		}
		else {
			Iterator<BindingSet> iter = lastResults.iterator();
			while (iter.hasNext()) {
				iter.next();
			}
			iter.remove();
		}
	}

	protected Collection<BindingSet> remove(NavigableMap<BindingSet, Collection<BindingSet>> map,
			BindingSet lastKey)
	{
		return map.remove(lastKey);
	}

	protected boolean add(BindingSet next, Collection<BindingSet> list)
		throws QueryEvaluationException
	{
		return list.add(next);
	}

	protected Collection<BindingSet> put(Map<BindingSet, Collection<BindingSet>> map, BindingSet next,
			Collection<BindingSet> list)
		throws QueryEvaluationException
	{
		return map.put(next, list);
	}

	protected NavigableMap<BindingSet, Collection<BindingSet>> makeOrderedMap(
			Comparator<BindingSet> comparator2)
	{
		return db.createTreeMap("order").comparator(comparator2).makeOrGet();
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
		this.db.close();
		super.handleClose();
	}
}
