/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.queryresult.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.openrdf.queryresult.TupleQueryResult;
import org.openrdf.queryresult.Solution;


/**
 * An implementation of the {@link TupleQueryResult} interface that stores Solutions
 * in an ordered collection.
 */
public class TupleQueryResultImpl implements TupleQueryResult {

	/*-----------*
	 * Variables *
	 *-----------*/

	private List<String> _bindingNames;

	private List<Solution> _solutions;

	private Iterator<Solution> _solutionIter;

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
		_solutions = new ArrayList<Solution>();
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
	 * @param solution
	 *        The solution to add to this query result.
	 */
	public void addSolution(Solution solution) {
		_solutions.add(solution);
	}

	public Solution nextSolution() {
		return iterator().next();
	}

	public Iterator<Solution> iterator() {
		if (_solutionIter == null) {
			_solutionIter = _solutions.iterator();
		}

		return _solutionIter;
	}

	public void close() {
		// no-op
	}

	public boolean isEmpty() {
		return _solutions.isEmpty();
	}
}
