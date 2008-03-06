/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

/**
 * An implementation of the {@link TupleQueryResult} interface that stores
 * Solutions in an ordered collection.
 */
public class TupleQueryResultImpl implements TupleQueryResult {

	/*-----------*
	 * Variables *
	 *-----------*/

	private List<String> _bindingNames;

	private List<BindingSet> _bindingSets;

	private CloseableIteration<BindingSet, QueryEvaluationException> _solutionIter;

	private boolean _ordered;

	private boolean _distinct;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a query result object with the supplied binding names.
	 * <em>The supplied list of binding names is assumed to be constant</em>;
	 * care should be taken that the contents of this list doesn't change after
	 * supplying it to this solution.
	 * 
	 * @param bindingNames
	 *        The binding names, in order of projection.
	 */
	public TupleQueryResultImpl(List<String> bindingNames) {
		_bindingNames = Collections.unmodifiableList(bindingNames);
		_bindingSets = new ArrayList<BindingSet>();
		_ordered = false;
		_distinct = false;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public List<String> getBindingNames() {
		return _bindingNames;
	}

	/**
	 * Sets the 'ordered' parameter for this query result.
	 */
	public void setOrdered(boolean ordered) {
		_ordered = ordered;
	}

	public boolean isOrdered() {
		return _ordered;
	}

	/**
	 * Sets the 'distinct' parameter for this query result.
	 */
	public void setDistinct(boolean distinct) {
		_distinct = distinct;
	}

	public boolean isDistinct() {
		return _distinct;
	}

	/**
	 * Adds a solution to this query result.
	 * 
	 * @param bindingSet
	 *        The solution to add to this query result.
	 */
	public void addSolution(BindingSet bindingSet) {
		_bindingSets.add(bindingSet);
	}

	public void close()
		throws QueryEvaluationException
	{
		if (_solutionIter != null) {
			_solutionIter.close();
		}
	}

	public boolean isEmpty() {
		return _bindingSets.isEmpty();
	}

	public boolean hasNext()
		throws QueryEvaluationException
	{
		if (_solutionIter == null) {
			_solutionIter = new CloseableIteratorIteration<BindingSet, QueryEvaluationException>(
					_bindingSets.iterator());
		}
		return _solutionIter.hasNext();
	}

	public BindingSet next()
		throws QueryEvaluationException
	{
		if (_solutionIter == null) {
			_solutionIter = new CloseableIteratorIteration<BindingSet, QueryEvaluationException>(
					_bindingSets.iterator());
		}
		return _solutionIter.next();
	}

	public void remove()
		throws QueryEvaluationException
	{
		if (_solutionIter == null) {
			_solutionIter = new CloseableIteratorIteration<BindingSet, QueryEvaluationException>(
					_bindingSets.iterator());
		}
		_solutionIter.remove();
	}
}
