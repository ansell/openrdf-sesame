/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;


/**
 * The LOCAL NAME function, which selects the local name of URIs.
 */
public class LocalName extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public LocalName(ValueExpr arg) {
		super(arg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	public String toString() {
		return "localName";
	}
}
