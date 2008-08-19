/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.List;

/**
 * A boolean OR operator operating on two boolean expressions.
 */
public class Or extends NaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Or() {
	}

	public Or(ValueExpr leftArg, ValueExpr rightArg) {
		super(leftArg, rightArg);
	}

	public Or(List<? extends ValueExpr> args) {
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
	public Or clone() {
		return (Or)super.clone();
	}
}
