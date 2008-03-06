/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

public class IsLiteral extends UnaryValueOperator {

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

	public IsLiteral clone() {
		return (IsLiteral)super.clone();
	}
}
