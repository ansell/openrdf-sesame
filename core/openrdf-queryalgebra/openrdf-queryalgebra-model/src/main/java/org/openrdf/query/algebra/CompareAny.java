/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import org.openrdf.query.algebra.Compare.CompareOp;

/**
 */
public class CompareAny extends CompareSubQueryValueOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private CompareOp _operator;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public CompareAny() {
	}

	public CompareAny(ValueExpr valueExpr, TupleExpr subQuery, CompareOp operator) {
		super(valueExpr, subQuery);
		setOperator(operator);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public CompareOp getOperator() {
		return _operator;
	}

	public void setOperator(CompareOp operator) {
		assert operator != null : "operator must not be null";
		_operator = operator;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public String toString() {
		return "COMPARE ANY (" + _operator.getSymbol() + ")";
	}

	public ValueExpr cloneValueExpr() {
		ValueExpr valueExpr = getArg().cloneValueExpr();
		TupleExpr subQuery = getSubQuery().cloneTupleExpr();
		CompareOp operator = getOperator();
		return new CompareAny(valueExpr, subQuery, operator);
	}
}
