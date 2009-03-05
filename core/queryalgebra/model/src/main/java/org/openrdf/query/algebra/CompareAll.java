/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import org.openrdf.query.algebra.Compare.CompareOp;

/**
 */
public class CompareAll extends CompareSubQueryValueOperator {

	private static final long serialVersionUID = -2946828255099788880L;

	/*-----------*
	 * Variables *
	 *-----------*/

	private CompareOp operator;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public CompareAll() {
	}

	public CompareAll(ValueExpr valueExpr, TupleExpr subQuery, CompareOp operator) {
		super(valueExpr, subQuery);
		setOperator(operator);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public CompareOp getOperator() {
		return operator;
	}

	public void setOperator(CompareOp operator) {
		assert operator != null : "operator must not be null";
		this.operator = operator;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public String getSignature() {
		return super.getSignature() + " (" + operator.getSymbol() + ")";
	}

	@Override
	public CompareAll clone() {
		return (CompareAll)super.clone();
	}
}
