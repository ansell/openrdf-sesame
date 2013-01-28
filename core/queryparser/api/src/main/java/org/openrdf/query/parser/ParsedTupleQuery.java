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

import org.openrdf.query.algebra.TupleExpr;

/**
 * A query formulated in the OpenRDF query algebra that produces a set of tuples
 * as its result.
 * 
 * @author Arjohn Kampman
 */
public class ParsedTupleQuery extends ParsedQuery {

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new tuple query. To complete this query, a tuple expression
	 * needs to be supplied to it using {@link #setTupleExpr(TupleExpr)}.
	 */
	public ParsedTupleQuery() {
		super();
	}

	/**
	 * Creates a new tuple query for the supplied tuple expression.
	 * 
	 * @param tupleExpr
	 *        A tuple expression representing the query, formulated in OpenRDF
	 *        Query Algebra objects.
	 */
	public ParsedTupleQuery(TupleExpr tupleExpr) {
		super(tupleExpr);
	}
	
	/**
	 * Creates a new tuple query for the supplied tuple expression.
	 * 
	 * @param tupleExpr
	 *        A tuple expression representing the query, formulated in OpenRDF
	 *        Query Algebra objects.
	 */
	public ParsedTupleQuery(String sourceString, TupleExpr tupleExpr) {
		super(sourceString, tupleExpr);
	}
}
