/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * A mathematical expression consisting an operator and two arguments.
 */
public class MathExpr extends BinaryValueOperator {

	/*---------------*
	 * enum Operator *
	 *---------------*/

	public enum MathOp {
		PLUS("+"),
		MINUS("-"),
		MULTIPLY("*"),
		DIVIDE("/");

		private String _symbol;

		MathOp(String symbol) {
			_symbol = symbol;
		}

		public String getSymbol() {
			return _symbol;
		}
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private MathOp _operator;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MathExpr() {
	}

	public MathExpr(ValueExpr leftArg, ValueExpr rightArg, MathOp operator) {
		super(leftArg, rightArg);
		setOperator(operator);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public MathOp getOperator() {
		return _operator;
	}

	public void setOperator(MathOp operator) {
		assert operator != null : "operator must not be null";
		_operator = operator;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public String toString() {
		return "MathExpr (" + _operator.getSymbol() + ")";
	}

	public ValueExpr cloneValueExpr() {
		ValueExpr leftArg = getLeftArg().cloneValueExpr();
		ValueExpr rightArg = getRightArg().cloneValueExpr();
		return new MathExpr(leftArg, rightArg, getOperator());
	}
}
