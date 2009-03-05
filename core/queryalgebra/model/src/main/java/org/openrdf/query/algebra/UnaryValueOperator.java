/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * An abstract superclass for unary value operators which, by definition, has
 * one argument.
 */
public abstract class UnaryValueOperator extends NaryValueOperator {

	private static final long serialVersionUID = -6030371229210829986L;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public UnaryValueOperator() {
	}

	/**
	 * Creates a new unary value operator.
	 * 
	 * @param arg
	 *        The operator's argument, must not be <tt>null</tt>.
	 */
	public UnaryValueOperator(ValueExpr arg) {
		super(arg);
	}

	@Override
	public void setArg(int idx, ValueExpr arg) {
		assert idx == 0;
		super.setArg(idx, arg);
	}

	/**
	 * Gets the only argument of this nary tuple operator.
	 * 
	 * @return The operator's argument.
	 */
	public ValueExpr getArg() {
		return getArg(0);
	}

	/**
	 * Sets the only argument of this nary tuple operator.
	 * 
	 * @param arg
	 *        The (new) argument for this operator, must not be <tt>null</tt>.
	 */
	public void setArg(ValueExpr arg) {
		setArg(0, arg);
	}

	@Override
	public UnaryValueOperator clone() {
		return (UnaryValueOperator)super.clone();
	}
}
