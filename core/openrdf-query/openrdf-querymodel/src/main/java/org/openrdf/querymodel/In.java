/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;


/**
 * Checks whether a certain value is contained in a set of results produced by a
 * query.
 */
public class In extends BooleanExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueExpr _valueExpr;

	private TupleExpr _subQuery;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public In(ValueExpr valueExpr, TupleExpr subQuery) {
		_valueExpr = valueExpr;
		_subQuery = subQuery;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getValueExpr() {
		return _valueExpr;
	}

	public TupleExpr getSubQuery() {
		return _subQuery;
	}

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	@Override
	public void visitChildren(QueryModelVisitor visitor)
	{
		_valueExpr.visit(visitor);
		_subQuery.visit(visitor);
	}

	public String toString() {
		return "IN";
	}
}
