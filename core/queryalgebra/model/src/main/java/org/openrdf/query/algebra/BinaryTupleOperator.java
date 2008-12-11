/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * An abstract superclass for binary tuple operators which, by definition, has
 * two arguments.
 */
public abstract class BinaryTupleOperator extends NaryTupleOperator implements TupleExpr {

	private static final long serialVersionUID = -2448897680662339140L;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BinaryTupleOperator() {
	}

	/**
	 * Creates a new binary tuple operator.
	 * 
	 * @param leftArg
	 *        The operator's left argument, must not be <tt>null</tt>.
	 * @param rightArg
	 *        The operator's right argument, must not be <tt>null</tt>.
	 */
	public BinaryTupleOperator(TupleExpr leftArg, TupleExpr rightArg) {
		super(leftArg, rightArg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the left argument of this binary tuple operator.
	 * 
	 * @return The operator's left argument.
	 */
	public TupleExpr getLeftArg() {
		return getArg(0);
	}

	/**
	 * Sets the left argument of this binary tuple operator.
	 * 
	 * @param leftArg
	 *        The (new) left argument for this operator, must not be
	 *        <tt>null</tt>.
	 */
	public void setLeftArg(TupleExpr leftArg) {
		setArg(0, leftArg);
	}

	/**
	 * Gets the right argument of this binary tuple operator.
	 * 
	 * @return The operator's right argument.
	 */
	public TupleExpr getRightArg() {
		return getArg(1);
	}

	/**
	 * Sets the right argument of this binary tuple operator.
	 * 
	 * @param rightArg
	 *        The (new) right argument for this operator, must not be
	 *        <tt>null</tt>.
	 */
	public void setRightArg(TupleExpr rightArg) {
		setArg(1, rightArg);
	}

	@Override
	public BinaryTupleOperator clone() {
		return (BinaryTupleOperator)super.clone();
	}
}
