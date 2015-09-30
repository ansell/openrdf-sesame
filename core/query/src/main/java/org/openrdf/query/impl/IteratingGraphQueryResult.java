/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
 * An iterating implementation of the {@link GraphQueryResult} interface.
 * 
 * @author Arjohn Kampman
 * @author Jeen Broekstra
 */
public class IteratingGraphQueryResult extends IterationWrapper<Statement, QueryEvaluationException>
		implements GraphQueryResult
{

	/*-----------*
	 * Variables *
	 *-----------*/

	private final Map<String, String> namespaces;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public IteratingGraphQueryResult(Map<String, String> namespaces, Iterable<? extends Statement> statements)
	{
		this(namespaces, statements.iterator());
	}

	public IteratingGraphQueryResult(Map<String, String> namespaces,
			Iterator<? extends Statement> statementIter)
	{
		this(namespaces, new CloseableIteratorIteration<Statement, QueryEvaluationException>(statementIter));
	}

	public IteratingGraphQueryResult(Map<String, String> namespaces,
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
