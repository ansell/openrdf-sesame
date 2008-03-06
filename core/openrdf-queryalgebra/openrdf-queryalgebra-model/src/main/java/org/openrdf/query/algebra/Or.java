/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * A boolean OR operator operating on two boolean expressions.
 */
public class Or extends BinaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Or() {
	}

	public Or(ValueExpr leftArg, ValueExpr rightArg) {
		super(leftArg, rightArg);
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
		return "OR";
	}

	public ValueExpr cloneValueExpr() {
		ValueExpr leftArg = getLeftArg().cloneValueExpr();
		ValueExpr rightArg = getRightArg().cloneValueExpr();
		return new Or(leftArg, rightArg);
	}
}
