/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

public abstract class CompareSubQueryValueOperator extends SubQueryValueOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	protected ValueExpr _arg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public CompareSubQueryValueOperator() {
	}

	public CompareSubQueryValueOperator(ValueExpr valueExpr, TupleExpr subQuery) {
		super(subQuery);
		setArg(valueExpr);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getArg() {
		return _arg;
	}

	public void setArg(ValueExpr valueExpr) {
		assert valueExpr != null : "valueExpr must not be null";
		_arg = valueExpr;
		valueExpr.setParentNode(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		_arg.visit(visitor);
		super.visitChildren(visitor);
	}

}
