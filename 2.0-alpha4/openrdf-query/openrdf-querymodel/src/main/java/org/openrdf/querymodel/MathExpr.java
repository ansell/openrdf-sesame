/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;



/**
 * A mathematical expression consisting an operator and two arguments.
 */
public class MathExpr extends BinaryValueOperator {

	/*---------------*
	 * enum Operator *
	 *---------------*/

	public enum Operator {
		PLUS("+"),
		MINUS("-"),
		MULTIPLY("*"),
		DIVIDE("/"),
		REMAINDER("%");

		private String _symbol;

		Operator(String symbol) {
			_symbol = symbol;
		}
		
		public String getSymbol() {
			return _symbol;
		}
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private Operator _operator;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MathExpr(ValueExpr leftArg, ValueExpr rightArg, Operator operator) {
		super(leftArg, rightArg);
		setOperator(operator);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Operator getOperator() {
		return _operator;
	}
	
	public void setOperator(Operator operator) {
		_operator = operator;
	}

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}
	
	public String toString() {
		return "MathExpr (" + _operator.getSymbol() + ")";
	}
}
