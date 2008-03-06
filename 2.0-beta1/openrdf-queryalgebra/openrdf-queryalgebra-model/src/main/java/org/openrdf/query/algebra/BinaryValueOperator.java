/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * An abstract superclass for binary value operators which, by definition, has
 * two arguments.
 */
public abstract class BinaryValueOperator extends QueryModelNodeBase implements ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's left argument.
	 */
	protected ValueExpr _leftArg;

	/**
	 * The operator's right argument.
	 */
	protected ValueExpr _rightArg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BinaryValueOperator() {
	}

	/**
	 * Creates a new binary value operator.
	 * 
	 * @param leftArg
	 *        The operator's left argument, must not be <tt>null</tt>.
	 * @param rightArg
	 *        The operator's right argument, must not be <tt>null</tt>.
	 */
	public BinaryValueOperator(ValueExpr leftArg, ValueExpr rightArg) {
		setLeftArg(leftArg);
		setRightArg(rightArg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the left argument of this binary value operator.
	 * 
	 * @return The operator's left argument.
	 */
	public ValueExpr getLeftArg() {
		return _leftArg;
	}

	/**
	 * Sets the left argument of this binary value operator.
	 * 
	 * @param leftArg
	 *        The (new) left argument for this operator, must not be
	 *        <tt>null</tt>.
	 */
	public void setLeftArg(ValueExpr leftArg) {
		assert leftArg != null : "leftArg must not be null";
		_leftArg = leftArg;
		_leftArg.setParentNode(this);
	}

	/**
	 * Gets the right argument of this binary value operator.
	 * 
	 * @return The operator's right argument.
	 */
	public ValueExpr getRightArg() {
		return _rightArg;
	}

	/**
	 * Sets the right argument of this binary value operator.
	 * 
	 * @param rightArg
	 *        The (new) right argument for this operator, must not be
	 *        <tt>null</tt>.
	 */
	public void setRightArg(ValueExpr rightArg) {
		assert rightArg != null : "rightArg must not be null";
		_rightArg = rightArg;
		_rightArg.setParentNode(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		_leftArg.visit(visitor);
		_rightArg.visit(visitor);
	}
}
