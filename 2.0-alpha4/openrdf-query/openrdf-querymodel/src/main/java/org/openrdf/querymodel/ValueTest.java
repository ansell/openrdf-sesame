/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;

public abstract class ValueTest extends BooleanExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueExpr _arg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ValueTest(ValueExpr arg) {
		_arg = arg;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getArg() {
		return _arg;
	}

	@Override
	public void visitChildren(QueryModelVisitor visitor)
	{
		_arg.visit(visitor);
	}
}
