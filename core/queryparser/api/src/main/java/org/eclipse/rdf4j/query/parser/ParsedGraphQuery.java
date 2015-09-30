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
package org.eclipse.rdf4j.query.parser;

import java.util.Collections;
import java.util.Map;

import org.eclipse.rdf4j.query.algebra.TupleExpr;

/**
 * A query forumalated in the OpenRDF query algebra that produces an RDF graph
 * (a set of statements) as its result.
 * 
 * @author Arjohn Kampman
 */
public class ParsedGraphQuery extends ParsedQuery {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Map<String, String> queryNamespaces;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new graph query. To complete this query, a tuple expression
	 * needs to be supplied to it using {@link #setTupleExpr(TupleExpr)}.
	 */
	public ParsedGraphQuery() {
		super();
	}

	/**
	 * Creates a new graph query. To complete this query, a tuple expression
	 * needs to be supplied to it using {@link #setTupleExpr(TupleExpr)}.
	 * 
	 * @param namespaces
	 *        A mapping of namespace prefixes to namespace names representing the
	 *        namespaces that are used in the query.
	 */
	public ParsedGraphQuery(Map<String, String> namespaces) {
		super();
		queryNamespaces = namespaces;
	}

	/**
	 * Creates a new graph query for the supplied tuple expression.
	 * 
	 * @param tupleExpr
	 *        A tuple expression representing the query, formulated in Sail Query
	 *        Model objects.
	 */
	public ParsedGraphQuery(TupleExpr tupleExpr) {
		super(tupleExpr);
	}
	

	/**
	 * Creates a new graph query for the supplied tuple expression.
	 * 
	 * @param tupleExpr
	 *        A tuple expression representing the query, formulated in Sail Query
	 *        Model objects.
	 */
	public ParsedGraphQuery(String sourceString, TupleExpr tupleExpr) {
		super(sourceString, tupleExpr);
	}

	/**
	 * Creates a new graph query.
	 * 
	 * @param tupleExpr
	 *        A tuple expression representing the query, formulated in Sail Query
	 *        Model objects.
	 * @param namespaces
	 *        A mapping of namespace prefixes to namespace names representing the
	 *        namespaces that are used in the query.
	 */
	public ParsedGraphQuery(TupleExpr tupleExpr, Map<String, String> namespaces) {
		this(tupleExpr);
		queryNamespaces = namespaces;
	}
	
	/**
	 * Creates a new graph query.
	 * 
	 * @param tupleExpr
	 *        A tuple expression representing the query, formulated in Sail Query
	 *        Model objects.
	 * @param namespaces
	 *        A mapping of namespace prefixes to namespace names representing the
	 *        namespaces that are used in the query.
	 */
	public ParsedGraphQuery(String sourceString, TupleExpr tupleExpr, Map<String, String> namespaces) {
		this(sourceString, tupleExpr);
		queryNamespaces = namespaces;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Map<String, String> getQueryNamespaces() {
		if (queryNamespaces != null) {
			return queryNamespaces;
		}
		else {
			return Collections.emptyMap();
		}
	}
}
