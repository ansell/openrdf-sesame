/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;

import java.util.Map;

/**
 * A GraphQuery is a query that produces an RDF graph (a set of statements) as
 * its result.
 */
public class GraphQuery extends Query {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Map<String, String> _queryNamespaces;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new graph query.
	 * 
	 * @param tupleExpr
	 *        A tuple expression representing the query, formulated in Sail Query
	 *        Model objects.
	 */
	public GraphQuery(TupleExpr tupleExpr) {
		super(tupleExpr);
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
	public GraphQuery(TupleExpr tupleExpr, Map<String, String> namespaces) {
		this(tupleExpr);
		_queryNamespaces = namespaces;
	}

	/*---------*
	 * Methods *
	 *---------*/
	public Map<String, String> getQueryNamespaces() {
		return _queryNamespaces;
	}
}
