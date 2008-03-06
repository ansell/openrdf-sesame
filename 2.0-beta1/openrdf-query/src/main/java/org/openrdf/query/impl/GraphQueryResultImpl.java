/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006.
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

	private Map<String, String> _namespaces;

	public GraphQueryResultImpl(Map<String, String> namespaces,
			CloseableIteration<Statement, QueryEvaluationException> statementIter)
	{
		super(statementIter);
		_namespaces = Collections.unmodifiableMap(namespaces);
	}

	public Map<String, String> getNamespaces() {
		return _namespaces;
	}

	public boolean isDistinct() {
		return false;
	}
}
