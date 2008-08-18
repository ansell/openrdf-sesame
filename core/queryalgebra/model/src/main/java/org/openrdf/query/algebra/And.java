/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.List;

/**
 * A boolean AND operator operating on two boolean expressions.
 */
public class And extends NaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public And() {
	}

	public And(ValueExpr leftArg, ValueExpr rightArg) {
		super(leftArg, rightArg);
	}

	public And(List<ValueExpr> args) {
		super(args);
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
	public And clone() {
		return (And)super.clone();
	}
}
