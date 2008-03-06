/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

public class IsURI extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public IsURI() {
	}

	public IsURI(ValueExpr arg) {
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

	public String toString() {
		return "isURI";
	}

	public ValueExpr cloneValueExpr() {
		return new IsURI(getArg().cloneValueExpr());
	}
}
