/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import info.aduna.iteration.CloseableIteratorIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;

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

	private Iteration<? extends BindingSet, QueryEvaluationException> _bindingSetIter;

	private boolean _distinct;

	private boolean _ordered;

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
	public TupleQueryResultImpl(List<String> bindingNames, Iterable<? extends BindingSet> bindingSets) {
		this(bindingNames, bindingSets.iterator());
	}

	public TupleQueryResultImpl(List<String> bindingNames, Iterator<? extends BindingSet> bindingSetIter) {
		this(bindingNames, new CloseableIteratorIteration<BindingSet, QueryEvaluationException>(bindingSetIter));
	}

	/**
	 * Creates a query result object with the supplied binding names.
	 * <em>The supplied list of binding names is assumed to be constant</em>;
	 * care should be taken that the contents of this list doesn't change after
	 * supplying it to this solution.
	 * 
	 * @param bindingNames
	 *        The binding names, in order of projection.
	 */
	public TupleQueryResultImpl(List<String> bindingNames,
			Iteration<? extends BindingSet, QueryEvaluationException> bindingSetIter)
	{
		// Don't allow modifications to the binding names when it is accessed
		// through getBindingNames:
		_bindingNames = Collections.unmodifiableList(bindingNames);
		_bindingSetIter = bindingSetIter;
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

	public void close()
		throws QueryEvaluationException
	{
		Iterations.closeCloseable(_bindingSetIter);
	}

	public boolean hasNext()
		throws QueryEvaluationException
	{
		return _bindingSetIter.hasNext();
	}

	public BindingSet next()
		throws QueryEvaluationException
	{
		return _bindingSetIter.next();
	}

	public void remove()
		throws QueryEvaluationException
	{
		_bindingSetIter.remove();
	}
}
