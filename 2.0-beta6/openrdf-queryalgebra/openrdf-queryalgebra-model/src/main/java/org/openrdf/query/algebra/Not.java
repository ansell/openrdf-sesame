/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * A boolean NOT operator operating on a boolean expressions.
 */
public class Not extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Not() {
	}

	public Not(ValueExpr arg) {
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

	public Not clone() {
		return (Not)super.clone();
	}
}
