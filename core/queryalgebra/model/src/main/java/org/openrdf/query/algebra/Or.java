/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.List;

/**
 * A boolean OR operator operating on two boolean expressions.
 */
public class Or extends NaryValueOperator {

	private static final long serialVersionUID = -9199477318547462027L;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Or() {
	}

	public Or(ValueExpr... args) {
		super(args);
	}

	public Or(Iterable<? extends ValueExpr> args) {
		super(args);
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
	public Or clone() {
		return (Or)super.clone();
	}
}
