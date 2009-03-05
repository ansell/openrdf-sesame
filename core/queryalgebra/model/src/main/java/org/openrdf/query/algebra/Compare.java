/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * A comparison between two values.
 */
public class Compare extends BinaryValueOperator {

	private static final long serialVersionUID = 1526058328982063476L;

	/*---------------*
	 * enum Operator *
	 *---------------*/

	public enum CompareOp {
		/** equal to */
		EQ("="),

		/** not equal to */
		NE("!="),

		/** lower than */
		LT("<"),

		/** lower than or equal to */
		LE("<="),

		/** greater than or equal to */
		GE(">="),

		/** greater than */
		GT(">");

		private String symbol;

		CompareOp(String symbol) {
			this.symbol = symbol;
		}

		public String getSymbol() {
			return symbol;
		}
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private CompareOp operator;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Compare() {
	}

	public Compare(ValueExpr leftArg, ValueExpr rightArg) {
		this(leftArg, rightArg, CompareOp.EQ);
	}

	public Compare(ValueExpr leftArg, ValueExpr rightArg, CompareOp operator) {
		super(leftArg, rightArg);
		setOperator(operator);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public CompareOp getOperator() {
		return operator;
	}

	public void setOperator(CompareOp operator) {
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
	public Compare clone() {
		return (Compare)super.clone();
	}
}
