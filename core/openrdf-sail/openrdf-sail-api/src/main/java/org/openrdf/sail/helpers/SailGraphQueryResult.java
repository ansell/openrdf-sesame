/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Statement;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;

public class SailGraphQueryResult implements GraphQueryResult {

	/*-----------*
	 * Variables *
	 *-----------*/

	private CloseableIteration<Statement, QueryEvaluationException> _iterator;

	private Map<String, String> _namespaces;

	private boolean _distinct;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SailGraphQueryResult(Map<String, String> namespaces,
			CloseableIteration<Statement, QueryEvaluationException> iterator)
	{
		_iterator = iterator;
		_distinct = false;
		if (namespaces == null) {
			_namespaces = Collections.emptyMap();
		}
		else {
			_namespaces = Collections.unmodifiableMap(new HashMap<String, String>(namespaces));
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setDistinct(boolean distinct) {
		_distinct = distinct;
	}

	public boolean isDistinct() {
		return _distinct;
	}

	public CloseableIteration<Statement, QueryEvaluationException> iterator() {
		return _iterator;
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

	public Map<String, String> getNamespaces() {
		return _namespaces;
	}


	public boolean hasNext()
		throws QueryEvaluationException
	{
		return _iterator.hasNext();
	}

	public Statement next()
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
