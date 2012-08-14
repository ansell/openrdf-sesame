/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The LABEL function, which selects the label of literals.
 * 
 * @author Arjohn Kampman
 */
public class Label extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Label() {
	}

	public Label(ValueExpr arg) {
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
	public boolean equals(Object other) {
		return other instanceof Label && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "Label".hashCode();
	}

	@Override
	public Label clone() {
		return (Label)super.clone();
	}
}
