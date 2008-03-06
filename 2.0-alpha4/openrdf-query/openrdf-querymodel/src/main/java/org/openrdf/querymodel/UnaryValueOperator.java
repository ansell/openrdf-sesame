/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;

/**
 * An abstract superclass for unary value operators which, by definition, has
 * one argument.
 */
public abstract class UnaryValueOperator extends ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's argument.
	 */
	protected ValueExpr _arg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new unary value operator.
	 * 
	 * @param arg
	 *        The operator's argument, must not be <tt>null</tt>.
	 */
	public UnaryValueOperator(ValueExpr arg) {
		setArg(arg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the argument of this unary value operator.
	 * 
	 * @return The operator's argument.
	 */
	public ValueExpr getArg() {
		return _arg;
	}

	/**
	 * Sets the argument of this unary value operator.
	 * 
	 * @param arg
	 *        The (new) argument for this operator, must not be <tt>null</tt>.
	 */
	public void setArg(ValueExpr arg) {
		assert arg != null : "arg must not be null";
		_arg = arg;
	}
	
	@Override
	public void visitChildren(QueryModelVisitor visitor) {
		_arg.visit(visitor);
	}
}
