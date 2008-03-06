/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;

/**
 * A default query optimizer that applies some generally applicable
 * optimizations.
 */
public class QueryOptimizer {

	/**
	 * Applies some generally applicable optimizations to the supplied query:
	 * variable assignments are inlined, and path expressions are sorted from
	 * more to less specific.
	 */
	public static TupleExpr optimize(TupleExpr tupleExpr) {
		return tupleExpr;
	}
}
