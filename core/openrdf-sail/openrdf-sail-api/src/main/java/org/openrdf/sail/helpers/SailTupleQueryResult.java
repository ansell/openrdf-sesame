/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

public class SailTupleQueryResult implements TupleQueryResult {

	/*-----------*
	 * Variables *
	 *-----------*/

	private List<String> _bindingNames;

	private CloseableIteration<? extends BindingSet, QueryEvaluationException> _iterator;

	private boolean _distinct;

	private boolean _ordered;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SailTupleQueryResult(Set<String> bindingNames,
			CloseableIteration<? extends BindingSet, QueryEvaluationException> iterator)
	{
		_bindingNames = Collections.unmodifiableList(new ArrayList<String>(bindingNames));
		_iterator = iterator;
		_distinct = false;
		_ordered = false;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public List<String> getBindingNames() {
		return _bindingNames;
	}

	public void setOrdered(boolean ordered) {
		_ordered = ordered;
	}

	public boolean isOrdered() {
		return _ordered;
	}

	public void setDistinct(boolean distinct) {
		_distinct = distinct;
	}

	public boolean isDistinct() {
		return _distinct;
	}

	public void close()
		throws QueryEvaluationException
	{
		_iterator.close();
	}

	public boolean isEmpty()
		throws QueryEvaluationException
	{
		return !_iterator.hasNext();
	}

	public boolean hasNext()
		throws QueryEvaluationException
	{
		return _iterator.hasNext();
	}

	public BindingSet next()
		throws QueryEvaluationException
	{
		return _iterator.next();
	}

	public void remove()
		throws QueryEvaluationException
	{
		_iterator.remove();
	}
}
