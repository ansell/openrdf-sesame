/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * Checks whether the wrapped Query produces any results.
 */
public class Exists extends SubQueryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Exists() {
	}

	public Exists(TupleExpr subQuery) {
		super(subQuery);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public String toString() {
		return "EXISTS";
	}

	public ValueExpr cloneValueExpr() {
		return new Exists(getSubQuery().cloneTupleExpr());
	}
}
