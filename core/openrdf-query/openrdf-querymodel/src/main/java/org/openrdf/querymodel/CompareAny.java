/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;


/**
 */
public class CompareAny extends BooleanExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueExpr _valueExpr;

	private TupleExpr _subQuery;

	private Compare.Operator _operator;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public CompareAny(ValueExpr valueExpr, TupleExpr subQuery, Compare.Operator operator) {
		_valueExpr = valueExpr;
		_subQuery = subQuery;
		_operator = operator;
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

	public Compare.Operator getOperator() {
		return _operator;
	}

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	@Override
	public void visitChildren(QueryModelVisitor visitor) {
		_valueExpr.visit(visitor);
		_subQuery.visit(visitor);
	}

	public String toString() {
		return "COMPARE ANY (" + _operator.getSymbol() + ")";
	}
}
