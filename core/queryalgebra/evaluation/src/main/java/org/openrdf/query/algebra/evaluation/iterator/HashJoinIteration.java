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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.LookAheadIteration;
import info.aduna.iterator.UnionIterator;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.impl.EmptyBindingSet;

/**
 * Generic hash join implementation suitable for use by Sail implementations.
 * @author MJAHale
 */
public class HashJoinIteration extends LookAheadIteration<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final CloseableIteration<BindingSet, QueryEvaluationException> leftIter;

	private volatile CloseableIteration<BindingSet, QueryEvaluationException> rightIter;

	private Iterator<BindingSet> scanList;

	private CloseableIteration<BindingSet, QueryEvaluationException> restIter;

	private Map<BindingSetHashKey, List<BindingSet>> hashTable;

	private String[] joinAttributes;

	private BindingSet currentScanElem;

	private Iterator<BindingSet> hashTableValues;

	private final boolean leftJoin;
	
	/*--------------*
	 * Constructors *
	 *--------------*/

	public HashJoinIteration(EvaluationStrategy strategy, Join join, BindingSet bindings)
		throws QueryEvaluationException
	{
		this(strategy, join, bindings, false);
	}

	public HashJoinIteration(EvaluationStrategy strategy, Join join, BindingSet bindings, boolean leftJoin)
			throws QueryEvaluationException
	{
		leftIter = strategy.evaluate(join.getLeftArg(), bindings);
		rightIter = strategy.evaluate(join.getRightArg(), bindings);

		Set<String> joinAttributeNames = join.getLeftArg().getBindingNames();
		joinAttributeNames.retainAll(join.getRightArg().getBindingNames());
		joinAttributes = joinAttributeNames.toArray(new String[joinAttributeNames.size()]);

		this.leftJoin = leftJoin;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected BindingSet getNextElement()
		throws QueryEvaluationException
	{
		if (hashTable == null) {
			setupHashTable();
		}

		while (currentScanElem == null) {
			if (scanList.hasNext()) {
				currentScanElem = nextFromCache(scanList);
			}
			else {
				disposeCache(scanList); // exhausted so can free

				if (restIter.hasNext()) {
					currentScanElem = restIter.next();
				}
				else {
					// no more elements available
					return null;
				}
			}

			if (currentScanElem instanceof EmptyBindingSet) {
				// the empty bindingset should be merged with all bindingset in the
				// hash table
				hashTableValues = new UnionIterator<BindingSet>(hashTable.values());
			}
			else {
				BindingSetHashKey key = BindingSetHashKey.create(joinAttributes, currentScanElem);
				List<BindingSet> hashValue = hashTable.get(key);
				if (hashValue != null) {
					hashTableValues = hashValue.iterator();
				}
				else if(leftJoin) {
					hashTableValues = Collections.singletonList(EmptyBindingSet.getInstance()).iterator();
				}
				else {
					currentScanElem = null;
					closeHashValue(hashTableValues);
					hashTableValues = null;
				}
			}
		}

		BindingSet nextHashTableValue = hashTableValues.next();

		QueryBindingSet result = new QueryBindingSet(currentScanElem);

		for (String name : nextHashTableValue.getBindingNames()) {
			if (!result.hasBinding(name)) {
				Value v = nextHashTableValue.getValue(name);
				if(v != null)
				{
					result.addBinding(name, v);
				}
			}
		}

		if (!hashTableValues.hasNext()) {
			// we've exhausted the current scanlist entry
			currentScanElem = null;
			closeHashValue(hashTableValues);
			hashTableValues = null;
		}

		return result;
	}

	@Override
	protected void handleClose()
		throws QueryEvaluationException
	{
		super.handleClose();

		leftIter.close();
		rightIter.close();

		if(hashTableValues != null)
		{
			closeHashValue(hashTableValues);
			hashTableValues = null;
		}
		if(scanList != null)
		{
			disposeCache(scanList);
			scanList = null;
		}
		if(hashTable != null)
		{
			disposeHashTable(hashTable);
			hashTable = null;
		}
	}

	private void setupHashTable()
		throws QueryEvaluationException
	{

		Collection<BindingSet> leftArgResults = makeIterationCache(leftIter);
		Collection<BindingSet> rightArgResults = makeIterationCache(rightIter);

		while (leftIter.hasNext() && rightIter.hasNext()) {
			add(leftArgResults, leftIter.next());
			add(rightArgResults, rightIter.next());
		}

		Collection<BindingSet> smallestResult = null;

		if (leftIter.hasNext()) { // leftArg is the greater relation
			smallestResult = rightArgResults;
			scanList = leftArgResults.iterator();
			restIter = leftIter;
		}
		else { // rightArg is the greater relation (or they are equal)
			smallestResult = leftArgResults;
			scanList = rightArgResults.iterator();
			restIter = rightIter;
		}

		// help free memory before allocating the hash table
		leftArgResults = null;
		rightArgResults = null;

		// create the hash table for our join
		// hash table will never be any bigger than smallestResult.size()
		hashTable = makeHashTable(smallestResult.size());
		int maxListSize = 1;
		for (BindingSet b : smallestResult) {
			BindingSetHashKey hashKey = BindingSetHashKey.create(joinAttributes, b);

			List<BindingSet> hashValue = hashTable.get(hashKey);
			if (hashValue == null) {
				hashValue = makeHashValue(maxListSize);
			}
			add(hashValue, b);
			// always do a put() in case the map implementation is not memory-based
			// e.g. it serializes the values
			putHashTableEntry(hashTable, hashKey, hashValue);

			maxListSize = Math.max(maxListSize, hashValue.size());
		}

	}

	protected void putHashTableEntry(Map<BindingSetHashKey, List<BindingSet>> hashTable, BindingSetHashKey hashKey,
			List<BindingSet> hashValue)
		throws QueryEvaluationException
	{
		// by default, we use a standard memory hash map
		// so we only need to do the put() if the list new
		if(hashValue.size() == 1)
		{
			hashTable.put(hashKey, hashValue);
		}
	}

	/**
	 * Utility methods to make it easier to inserted custom store dependent list
	 * 
	 * @return list
	 */
	protected Collection<BindingSet> makeIterationCache(CloseableIteration<BindingSet, QueryEvaluationException> iter) {
		return new ArrayList<BindingSet>();
	}

	/**
	 * Utility methods to make it easier to inserted custom store dependent maps
	 * 
	 * @return map
	 */
	protected Map<BindingSetHashKey, List<BindingSet>> makeHashTable(int initialSize) {
		Map<BindingSetHashKey, List<BindingSet>> hashTable;
		if(joinAttributes.length > 0)
		{
			// we should probably adjust for the load factor
			// but we are only one rehash away and this might save a bit of memory
			// when we have more than one value per entry
			hashTable = new HashMap<BindingSetHashKey, List<BindingSet>>(initialSize);
		}
		else
		{
			hashTable = Collections.<BindingSetHashKey,List<BindingSet>>singletonMap(BindingSetHashKey.EMPTY, new ArrayList<BindingSet>(initialSize));
		}
		return hashTable;
	}

	/**
	 * Utility methods to make it easier to inserted custom store dependent list
	 * 
	 * @return list
	 */
	protected List<BindingSet> makeHashValue(int currentMaxListSize) {
		// we pick an initial size that means we may only have to resize once
		// while saving memory in the case that the list doesn't reach max size
		return new ArrayList<BindingSet>(currentMaxListSize/2+1);
	}

	/**
	 * Utility methods to clear-up in case not using in-memory cache
	 * 
	 * @return map
	 */
	protected void disposeCache(Iterator<BindingSet> iter)
	{
	}

	/**
	 * Utility methods to clear-up in case not using in-memory hash table
	 * 
	 * @return map
	 */
	protected void disposeHashTable(Map<BindingSetHashKey, List<BindingSet>> map)
	{
	}

	/**
	 * Utility methods to clear-up in case not using in-memory hash table
	 * 
	 * @return map
	 */
	protected <E> void closeHashValue(Iterator<E> iter)
	{
	}

	// hooks for LimitedSizeHashJoinIterator

	protected <E> E nextFromCache(Iterator<E> iter)
	{
		return iter.next();
	}

	protected <E> void add(Collection<E> col, E value)
		throws QueryEvaluationException
	{
		col.add(value);
	}

	protected <E> void addAll(Collection<E> col, List<E> values)
		throws QueryEvaluationException
	{
		col.addAll(values);
	}
}
