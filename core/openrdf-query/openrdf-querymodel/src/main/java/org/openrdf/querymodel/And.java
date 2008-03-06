/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;


/**
 * A boolean AND operator operating on two boolean expressions.
 */
public class And extends BooleanExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	private BooleanExpr _leftArg;

	private BooleanExpr _rightArg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public And(BooleanExpr leftArg, BooleanExpr rightArg) {
		setLeftArg(leftArg);
		setRightArg(rightArg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public BooleanExpr getLeftArg() {
		return _leftArg;
	}

	public void setLeftArg(BooleanExpr leftArg) {
		_leftArg = leftArg;
	}

	public BooleanExpr getRightArg() {
		return _rightArg;
	}

	public void setRightArg(BooleanExpr rightArg) {
		_rightArg = rightArg;
	}

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	@Override
	public void visitChildren(QueryModelVisitor visitor)
	{
		_leftArg.visit(visitor);
		_rightArg.visit(visitor);
	}

	public String toString() {
		return "AND";
	}
}
