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

		private String symbol;

		MathOp(String symbol) {
			this.symbol = symbol;
		}

		public String getSymbol() {
			return symbol;
		}
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private MathOp operator;

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
		return operator;
	}

	public void setOperator(MathOp operator) {
		assert operator != null : "operator must not be null";
		this.operator = operator;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public String getSignature() {
		return super.getSignature() + " (" + operator.getSymbol() + ")";
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof MathExpr && super.equals(other)) {
			MathExpr o = (MathExpr)other;
			return operator.equals(o.getOperator());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ operator.hashCode();
	}

	@Override
	public MathExpr clone() {
		return (MathExpr)super.clone();
	}
}
