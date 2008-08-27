/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;

import org.openrdf.StoreException;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;

/**
 * A generic implementation of the {@link TupleQueryResult} interface.
 */
public class TupleQueryResultImpl implements TupleQueryResult {

	/*-----------*
	 * Variables *
	 *-----------*/

	private List<String> bindingNames;

	private Iteration<? extends BindingSet, StoreException> bindingSetIter;

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
		this(bindingNames, new CloseableIteratorIteration<BindingSet, StoreException>(bindingSetIter));
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
			CloseableIteration<? extends BindingSet, StoreException> bindingSetIter)
	{
		// Don't allow modifications to the binding names when it is accessed
		// through getBindingNames:
		this.bindingNames = Collections.unmodifiableList(bindingNames);
		this.bindingSetIter = bindingSetIter;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public List<String> getBindingNames() {
		return bindingNames;
	}

	public void close()
		throws StoreException
	{
		Iterations.closeCloseable(bindingSetIter);
	}

	public boolean hasNext()
		throws StoreException
	{
		return bindingSetIter.hasNext();
	}

	public BindingSet next()
		throws StoreException
	{
		return bindingSetIter.next();
	}

	public void remove()
		throws StoreException
	{
		bindingSetIter.remove();
	}
}
