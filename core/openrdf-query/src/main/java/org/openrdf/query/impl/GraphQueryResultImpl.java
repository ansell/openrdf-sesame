/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.impl;

import java.util.Collections;
import java.util.Map;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.IterationWrapper;

import org.openrdf.model.Statement;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;

/**
 * An utility implementation of the {@link GraphQueryResult} interface.
 * 
 * @author Arjohn Kampman
 * @author jeen
 */
public class GraphQueryResultImpl extends IterationWrapper<Statement, QueryEvaluationException> implements
		GraphQueryResult
{

	/*-----------*
	 * Variables *
	 *-----------*/

	private Map<String, String> _namespaces;

	private boolean _distinct;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public GraphQueryResultImpl(Map<String, String> namespaces,
			CloseableIteration<Statement, QueryEvaluationException> statementIter)
	{
		this(namespaces, statementIter, false);
	}

	@Deprecated
	public GraphQueryResultImpl(Map<String, String> namespaces,
			CloseableIteration<Statement, QueryEvaluationException> statementIter, boolean distinct)
	{
		super(statementIter);
		_namespaces = Collections.unmodifiableMap(namespaces);
		_distinct = distinct;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Map<String, String> getNamespaces() {
		return _namespaces;
	}

	@Deprecated
	public void setDistinct(boolean distinct)
	{
		_distinct = distinct;
	}

	@Deprecated
	public boolean isDistinct()
	{
		return _distinct;
	}
}
