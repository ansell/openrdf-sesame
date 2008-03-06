/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;



/**
 * A TupleQuery is a query that produces a set of tuples as its result.
 */
public class TupleQuery extends Query {

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TupleQuery.
	 * 
	 * @param tupleExpr
	 *        A tuple expression representing the query, formulated in Sail Query
	 *        Model objects.
	 */
	public TupleQuery(TupleExpr tupleExpr) {
		super(tupleExpr);
	}
}
