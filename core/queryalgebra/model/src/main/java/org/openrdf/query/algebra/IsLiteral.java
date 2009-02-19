/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

public class IsLiteral extends UnaryValueOperator {

	private static final long serialVersionUID = 4676127518119593726L;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public IsLiteral() {
	}

	public IsLiteral(ValueExpr arg) {
		super(arg);
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
	public IsLiteral clone() {
		return (IsLiteral)super.clone();
	}
}
