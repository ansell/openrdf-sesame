/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;


/**
 * The LABEL function, which selects the label of literals.
 */
public class Label extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Label(ValueExpr arg) {
		super(arg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	public String toString() {
		return "label";
	}
}
