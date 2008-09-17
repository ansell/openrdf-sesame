/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * An abstract superclass for binary value operators which, by definition, has
 * two arguments.
 */
public abstract class BinaryValueOperator extends NaryValueOperator implements ValueExpr {

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
		super(leftArg, rightArg);
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
		return getArg(0);
	}

	/**
	 * Sets the left argument of this binary value operator.
	 * 
	 * @param leftArg
	 *        The (new) left argument for this operator, must not be
	 *        <tt>null</tt>.
	 */
	public void setLeftArg(ValueExpr leftArg) {
		setArg(0, leftArg);
	}

	/**
	 * Gets the right argument of this binary value operator.
	 * 
	 * @return The operator's right argument.
	 */
	public ValueExpr getRightArg() {
		return getArg(1);
	}

	/**
	 * Sets the right argument of this binary value operator.
	 * 
	 * @param rightArg
	 *        The (new) right argument for this operator, must not be
	 *        <tt>null</tt>.
	 */
	public void setRightArg(ValueExpr rightArg) {
		setArg(1, rightArg);
	}
	
	@Override
	public BinaryValueOperator clone() {
		return (BinaryValueOperator)super.clone();
	}
}
