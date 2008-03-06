/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;

/**
 * Abstract super class of all query types.
 */
public abstract class Query {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * A tuple expression representing the actual query, formulated in Sail Query
	 * Model objects.
	 */
	protected TupleExpr _tupleExpr;

	protected String _queryString;

	private QueryLanguage _queryLanguage;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new query object.
	 * 
	 * @param tupleExpr
	 *        The tuple expression underlying this query.
	 */
	public Query(TupleExpr tupleExpr) {
		_tupleExpr = tupleExpr;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the tuple expression underlying this query.
	 */
	public TupleExpr getTupleExpr() {
		return _tupleExpr;
	}

	/**
	 * Returns a string representation of the query that can be used for
	 * debugging.
	 */
	public String toString() {
		return _tupleExpr.toString();
	}

	/**
	 * Gets the query string that represents this query, expressed in the query
	 * language specified by {@link #getQueryLanguage()}.
	 * 
	 * @return the query string.
	 */
	public String getQueryString() {
		return _queryString;
	}

	public QueryLanguage getQueryLanguage() {
		return _queryLanguage;
	}

	/**
	 * Setting is allowed for the parser
	 */
	public void setQueryLanguage(QueryLanguage queryLanguage) {
		_queryLanguage = queryLanguage;
	}

	/**
	 * Setting is allowed for the parser
	 */
	public void setQueryString(String queryString) {
		_queryString = queryString;
	}

}
