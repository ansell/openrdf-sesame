/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The LOCAL NAME function, which selects the local name of URIs.
 */
public class LocalNameFunc extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public LocalNameFunc() {
	}

	public LocalNameFunc(ValueExpr arg) {
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
		return "localName";
	}

	public ValueExpr cloneValueExpr() {
		return new LocalNameFunc(getArg().cloneValueExpr());
	}
}
