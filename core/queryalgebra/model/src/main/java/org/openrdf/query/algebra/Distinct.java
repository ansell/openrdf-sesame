/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

public class Distinct extends UnaryTupleOperator {

	private static final long serialVersionUID = 6332322848583834708L;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Distinct() {
	}

	public Distinct(TupleExpr arg) {
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
	public Distinct clone() {
		return (Distinct)super.clone();
	}
}
