/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser;

import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.TupleExpr;

/**
 * A query formulated in the OpenRDF query algebra that produces a set of tuples
 * as its result.
 * 
 * @author Arjohn Kampman
 */
public class TupleQueryModel extends QueryModel {

	private static final long serialVersionUID = -290217832836264914L;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new tuple query. To complete this query, a tuple expression
	 * needs to be supplied to it using {@link #setArg(TupleExpr)}.
	 */
	public TupleQueryModel() {
		super();
	}

	/**
	 * Creates a new tuple query for the supplied tuple expression.
	 * 
	 * @param tupleExpr
	 *        A tuple expression representing the query, formulated in OpenRDF
	 *        Query Algebra objects.
	 */
	public TupleQueryModel(TupleExpr tupleExpr) {
		super(tupleExpr);
	}
}
