/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The NAMESPACE function, which selects the namespace of URIs.
 */
public class NamespaceFunc extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NamespaceFunc() {
	}

	public NamespaceFunc(ValueExpr arg) {
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
		return "namespace";
	}

	public ValueExpr cloneValueExpr() {
		return new NamespaceFunc(getArg().cloneValueExpr());
	}
}
