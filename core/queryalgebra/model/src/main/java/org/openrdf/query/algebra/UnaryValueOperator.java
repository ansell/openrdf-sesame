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
	public UnaryValueOperator clone() {
		return (UnaryValueOperator)super.clone();
	}
}
