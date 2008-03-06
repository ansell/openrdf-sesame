/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser;

import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;

/**
 * Abstract super class of all query types that a query parser can generate.
 * 
 * @author Arjohn Kampman
 */
public abstract class ParsedQuery {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * A tuple expression representing the actual query, formulated in OpenRDF
	 * Query Algebra objects.
	 */
	private TupleExpr tupleExpr;

	/**
	 * The dataset that was specified in the query, if any.
	 */
	private Dataset dataset;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new query object. To complete this query, a tuple expression
	 * needs to be supplied to it using {@link #setTupleExpr(TupleExpr)}.
	 */
	public ParsedQuery() {
	}

	/**
	 * Creates a new query object.
	 * 
	 * @param tupleExpr
	 *        The tuple expression underlying this query.
	 */
	public ParsedQuery(TupleExpr tupleExpr) {
		setTupleExpr(tupleExpr);
	}

	/**
	 * Creates a new query object.
	 * 
	 * @param tupleExpr
	 *        The tuple expression underlying this query.
	 */
	public ParsedQuery(TupleExpr tupleExpr, Dataset dataset) {
		this(tupleExpr);
		setDataset(dataset);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the tuple expression underlying this query.
	 */
	public void setTupleExpr(TupleExpr tupleExpr) {
		assert tupleExpr != null : "tupleExpr must not be null";
		this.tupleExpr = tupleExpr;
	}

	/**
	 * Gets the tuple expression underlying this query.
	 */
	public TupleExpr getTupleExpr() {
		return tupleExpr;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	/**
	 * Returns a string representation of the query that can be used for
	 * debugging.
	 */
	@Override
	public String toString()
	{
		return tupleExpr.toString();
	}
}
