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

	private CompareOp operator;

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
	public boolean equals(Object other) {
		if (other instanceof CompareAny && super.equals(other)) {
			CompareAny o = (CompareAny)other;
			return operator.equals(o.getOperator());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ operator.hashCode() ^ "CompareAny".hashCode();
	}

	@Override
	public CompareAny clone() {
		return (CompareAny)super.clone();
	}
}
