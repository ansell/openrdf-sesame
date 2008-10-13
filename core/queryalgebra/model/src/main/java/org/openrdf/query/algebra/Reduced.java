/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

public class Reduced extends UnaryTupleOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Reduced() {
	}

	public Reduced(TupleExpr arg) {
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
	public Reduced clone() {
		return (Reduced)super.clone();
	}
}
