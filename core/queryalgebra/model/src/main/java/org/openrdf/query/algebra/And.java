/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * A boolean AND operator operating on two boolean expressions.
 */
public class And extends BinaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public And() {
	}

	public And(ValueExpr leftArg, ValueExpr rightArg) {
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

	@Override
	public boolean equals(Object other) {
		return other instanceof And && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "And".hashCode();
	}

	@Override
	public And clone() {
		return (And)super.clone();
	}
}
