/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The STR function, which selects the label of literals or the string value of
 * the URI.
 */
public class StrFunc extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public StrFunc() {
	}

	public StrFunc(ValueExpr arg) {
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
		return "str";
	}

	public ValueExpr cloneValueExpr() {
		return new StrFunc(getArg().cloneValueExpr());
	}
}
