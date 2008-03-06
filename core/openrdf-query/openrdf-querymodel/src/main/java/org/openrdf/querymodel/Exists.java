/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;


/**
 * Checks whether the wrapped Query produces any results.
 */
public class Exists extends BooleanExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	private TupleExpr _subQuery;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Exists(TupleExpr subQuery) {
		_subQuery = subQuery;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public TupleExpr getSubQuery() {
		return _subQuery;
	}

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	@Override
	public void visitChildren(QueryModelVisitor visitor) {
		_subQuery.visit(visitor);
	}

	public String toString() {
		return "EXISTS";
	}
}
