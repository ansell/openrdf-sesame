/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser;

import java.util.Collections;
import java.util.Map;

import org.openrdf.query.algebra.TupleExpr;

/**
 * Abstract superclass for all update operation formulated in the OpenRDF query algebra.
 * 
 * @author Jeen Broekstra
 */
public class ParsedUpdate extends ParsedOperation {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Map<String, String> namespaces;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new update. To complete this update, a tuple expression
	 * needs to be supplied to it using {@link #setTupleExpr(TupleExpr)}.
	 */
	public ParsedUpdate() {
		super();
	}

	/**
	 * Creates a new update. To complete this query, a tuple expression
	 * needs to be supplied to it using {@link #setTupleExpr(TupleExpr)}.
	 * 
	 * @param namespaces
	 *        A mapping of namespace prefixes to namespace names representing the
	 *        namespaces that are used in the update.
	 */
	public ParsedUpdate(Map<String, String> namespaces) {
		super();
		this.namespaces = namespaces;
	}

	/**
	 * Creates a new update for the supplied tuple expression.
	 * 
	 * @param tupleExpr
	 *        A tuple expression representing the update, formulated in Sail Query
	 *        Model objects.
	 */
	public ParsedUpdate(TupleExpr tupleExpr) {
		super(tupleExpr);
	}

	/**
	 * Creates a new update.
	 * 
	 * @param tupleExpr
	 *        A tuple expression representing the update, formulated in Sail Query
	 *        Model objects.
	 * @param namespaces
	 *        A mapping of namespace prefixes to namespace names representing the
	 *        namespaces that are used in the update.
	 */
	public ParsedUpdate(TupleExpr tupleExpr, Map<String, String> namespaces) {
		this(tupleExpr);
		this.namespaces = namespaces;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Map<String, String> getNamespaces() {
		if (namespaces != null) {
			return namespaces;
		}
		else {
			return Collections.emptyMap();
		}
	}
}
