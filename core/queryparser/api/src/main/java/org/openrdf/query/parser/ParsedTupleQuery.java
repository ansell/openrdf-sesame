/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
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
