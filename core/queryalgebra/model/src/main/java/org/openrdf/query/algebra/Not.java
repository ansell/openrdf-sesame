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

	private static final long serialVersionUID = 6840464078544033786L;

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

	@Override
	public Not clone() {
		return (Not)super.clone();
	}
}
