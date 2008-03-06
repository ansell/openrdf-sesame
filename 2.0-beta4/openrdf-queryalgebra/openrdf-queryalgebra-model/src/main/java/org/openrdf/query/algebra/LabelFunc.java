/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The LABEL function, which selects the label of literals.
 */
public class LabelFunc extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public LabelFunc() {
	}

	public LabelFunc(ValueExpr arg) {
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
		return "label";
	}

	public ValueExpr cloneValueExpr() {
		return new LabelFunc(getArg().cloneValueExpr());
	}
}
