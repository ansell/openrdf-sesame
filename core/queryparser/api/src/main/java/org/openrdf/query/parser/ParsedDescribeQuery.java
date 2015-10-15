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
package org.openrdf.query.parser;

import java.util.Collections;
import java.util.Map;

import org.openrdf.query.algebra.TupleExpr;

/**
 * A ParsedGraphQuery to identify DESCRIBE queries.
 */
public class ParsedDescribeQuery extends ParsedGraphQuery {

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new graph query. To complete this query, a tuple expression
	 * needs to be supplied to it using {@link #setTupleExpr(TupleExpr)}.
	 */
	public ParsedDescribeQuery() {
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
	public ParsedDescribeQuery(Map<String, String> namespaces) {
		super(namespaces);
	}

	/**
	 * Creates a new graph query for the supplied tuple expression.
	 * 
	 * @param tupleExpr
	 *        A tuple expression representing the query, formulated in Sail Query
	 *        Model objects.
	 */
	public ParsedDescribeQuery(TupleExpr tupleExpr) {
		super(tupleExpr);
	}
	

	/**
	 * Creates a new graph query for the supplied tuple expression.
	 * 
	 * @param tupleExpr
	 *        A tuple expression representing the query, formulated in Sail Query
	 *        Model objects.
	 */
	public ParsedDescribeQuery(String sourceString, TupleExpr tupleExpr) {
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
	public ParsedDescribeQuery(TupleExpr tupleExpr, Map<String, String> namespaces) {
		super(tupleExpr, namespaces);
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
	public ParsedDescribeQuery(String sourceString, TupleExpr tupleExpr, Map<String, String> namespaces) {
		super(sourceString, tupleExpr, namespaces);
	}
}
