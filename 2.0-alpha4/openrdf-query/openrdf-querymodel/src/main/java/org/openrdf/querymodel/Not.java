/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;


/**
 * A boolean NOT operator operating on a boolean expressions.
 */
public class Not extends BooleanExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	private BooleanExpr _arg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Not(BooleanExpr arg) {
		setArg(arg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public BooleanExpr getArg() {
		return _arg;
	}

	public void setArg(BooleanExpr arg) {
		_arg = arg;
	}

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}
	
	@Override
	public void visitChildren(QueryModelVisitor visitor) {
		_arg.visit(visitor);
	}

	public String toString() {
		return "NOT";
	}
}
