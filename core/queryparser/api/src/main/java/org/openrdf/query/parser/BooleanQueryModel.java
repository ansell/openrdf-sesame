/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser;

import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.TupleExpr;

/**
 * A query formulated in the OpenRDF query algebra that produces a boolean value
 * as its result.
 * 
 * @author Arjohn Kampman
 */
public class BooleanQueryModel extends QueryModel {

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new boolean query. To complete this query, a tuple expression
	 * needs to be supplied to it using {@link #setTupleExpr(TupleExpr)}.
	 */
	public BooleanQueryModel() {
		super();
	}

	/**
	 * Creates a new boolean query for the supplied tuple expression.
	 * 
	 * @param tupleExpr
	 *        A tuple expression representing the query, formulated in OpenRDF
	 *        Query Algebra objects.
	 */
	public BooleanQueryModel(TupleExpr tupleExpr) {
		super(tupleExpr);
	}
}
