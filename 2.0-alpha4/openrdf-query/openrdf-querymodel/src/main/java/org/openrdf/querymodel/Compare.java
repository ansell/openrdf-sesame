/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;




/**
 * A comparison between two values.
 */
public class Compare extends BooleanExpr {

	/*---------------*
	 * enum Operator *
	 *---------------*/

	public enum Operator {
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
 
	private ValueExpr _leftArg;
	private ValueExpr _rightArg;
	private Operator _operator;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Compare(ValueExpr leftArg, ValueExpr rightArg) {
		this(leftArg, rightArg, Operator.EQ);
	}

	public Compare(ValueExpr leftArg, ValueExpr rightArg, Operator operator) {
		setLeftArg(leftArg);
		setRightArg(rightArg);
		setOperator(operator);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getLeftArg() {
		return _leftArg;
	}

	public void setLeftArg(ValueExpr leftArg) {
		_leftArg = leftArg;
	}
	
	public ValueExpr getRightArg() {
		return _rightArg;
	}

	public void setRightArg(ValueExpr rightArg) {
		_rightArg = rightArg;
	}

	public Operator getOperator() {
		return _operator;
	}

	public void setOperator(Operator operator) {
		_operator = operator;
	}

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	@Override
	public void visitChildren(QueryModelVisitor visitor) {
		_leftArg.visit(visitor);
		_rightArg.visit(visitor);
	}

	public String toString() {
		return "COMPARE (" + _operator.getSymbol() + ")";
	}
}
