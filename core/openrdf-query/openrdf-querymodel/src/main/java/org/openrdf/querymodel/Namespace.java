/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;


/**
 * The NAMESPACE function, which selects the namespace of URIs.
 */
public class Namespace extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Namespace(ValueExpr arg) {
		super(arg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	public String toString() {
		return "namespace";
	}
}
