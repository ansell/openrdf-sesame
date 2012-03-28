/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.BottomUpJoin;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.impl.EmptyBindingSet;

/**
 * Join Iterator that executes a basic bottom-up hash-join algorithm. To be used
 * in cases where interleaved iteration joining is not appropriate (e.g. when
 * the join arguments are subselects).
 * 
 * @author jeen
 */
public class BottomUpJoinIterator extends LookAheadIteration<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final CloseableIteration<BindingSet, QueryEvaluationException> leftIter;

	private volatile CloseableIteration<BindingSet, QueryEvaluationException> rightIter;

	private List<BindingSet> scanList;

	private CloseableIteration<BindingSet, QueryEvaluationException> restIter;

	private HashMap<BindingSet, ArrayList<BindingSet>> hashTable;

	private Set<String> joinAttributes;

	private BindingSet currentScanElem;

	private ArrayList<BindingSet> hashTableValues;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BottomUpJoinIterator(EvaluationStrategy strategy, BottomUpJoin join, BindingSet bindings)
		throws QueryEvaluationException
	{
		leftIter = strategy.evaluate(join.getLeftArg(), bindings);
		rightIter = strategy.evaluate(join.getRightArg(), bindings);

		joinAttributes = join.getLeftArg().getBindingNames();
		joinAttributes.retainAll(join.getRightArg().getBindingNames());

		hashTable = new HashMap<BindingSet, ArrayList<BindingSet>>();

		List<BindingSet> leftArgResults = new ArrayList<BindingSet>();
		List<BindingSet> rightArgResults = new ArrayList<BindingSet>();

		while (leftIter.hasNext() && rightIter.hasNext()) {
			BindingSet b = leftIter.next();
			leftArgResults.add(b);
			b = rightIter.next();
			rightArgResults.add(b);
		}

		List<BindingSet> smallestResult = null;

		if (leftIter.hasNext()) { // leftArg is the greater relation
			smallestResult = rightArgResults;
			scanList = leftArgResults;
			restIter = leftIter;
		}
		else { // rightArg is the greater relation (or they are equal)
			smallestResult = leftArgResults;
			scanList = rightArgResults;
			restIter = rightIter;
		}

		// create the hash table for our join
		for (BindingSet b : smallestResult) {
			BindingSet hashKey = calcKey(b, joinAttributes);

			ArrayList<BindingSet> hashValue = null;
			if (hashTable.containsKey(hashKey)) {
				hashValue = hashTable.get(hashKey);
			}
			else {
				hashValue = new ArrayList<BindingSet>();
			}
			hashValue.add(b);
			hashTable.put(hashKey, hashValue);
		}

	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected BindingSet getNextElement()
		throws QueryEvaluationException
	{
		while (currentScanElem == null) {
			if (scanList.size() > 0) {
				currentScanElem = scanList.remove(0);
			}
			else {
				if (restIter.hasNext()) {
					currentScanElem = restIter.next();
				}
				else {
					// no more elements available
					return null;
				}
			}

			if (currentScanElem instanceof EmptyBindingSet) {
				// the empty bindingset should be merged with all bindingset in the hash table
				hashTableValues = new ArrayList<BindingSet>();
				for (BindingSet key : hashTable.keySet()) {
					hashTableValues.addAll(hashTable.get(key));
				}
			}
			else {
				BindingSet key = calcKey(currentScanElem, joinAttributes);

				if (hashTable.containsKey(key)) {
					hashTableValues = new ArrayList<BindingSet>(hashTable.get(key));
				}
				else {
					currentScanElem = null;
					hashTableValues = null;
				}
			}
		}

		BindingSet nextHashTableValue = hashTableValues.remove(0);

		QueryBindingSet result = new QueryBindingSet(currentScanElem);

		for (String name : nextHashTableValue.getBindingNames()) {
			Binding b = nextHashTableValue.getBinding(name);
			if (!result.hasBinding(name)) {
				result.addBinding(b);
			}
		}

		if (hashTableValues.size() == 0) {
			// we've exhausted the current scanlist entry
			currentScanElem = null;
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
		
		hashTable = null;
		hashTableValues = null;
		scanList = null;
	}

	private BindingSet calcKey(BindingSet bindings, Set<String> commonVars) {
		QueryBindingSet q = new QueryBindingSet();
		for (String varName : commonVars) {
			Binding b = bindings.getBinding(varName);
			if (b != null) {
				q.addBinding(b);
			}
		}
		return q;
	}
}
