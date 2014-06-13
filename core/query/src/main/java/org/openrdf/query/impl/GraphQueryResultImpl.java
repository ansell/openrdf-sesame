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
package org.openrdf.query.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;
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

	private final Map<String, String> namespaces;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public GraphQueryResultImpl(Map<String, String> namespaces, Iterable<? extends Statement> statements) {
		this(namespaces, statements.iterator());
	}

	public GraphQueryResultImpl(Map<String, String> namespaces, Iterator<? extends Statement> statementIter) {
		this(namespaces, new CloseableIteratorIteration<Statement, QueryEvaluationException>(statementIter));
	}

	public GraphQueryResultImpl(Map<String, String> namespaces,
			CloseableIteration<? extends Statement, ? extends QueryEvaluationException> statementIter)
	{
		super(statementIter);
		this.namespaces = Collections.unmodifiableMap(namespaces);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Map<String, String> getNamespaces() {
		return namespaces;
	}
}
