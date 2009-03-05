/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.Set;

/**
 * An abstract superclass for unary tuple operators which, by definition, has
 * one argument.
 */
public abstract class UnaryTupleOperator extends NaryTupleOperator {

	private static final long serialVersionUID = -3843189239446570796L;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public UnaryTupleOperator() {
	}

	/**
	 * Creates a new unary tuple operator.
	 * 
	 * @param arg
	 *        The operator's argument, must not be <tt>null</tt>.
	 */
	public UnaryTupleOperator(TupleExpr arg) {
		super(arg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void setArg(int idx, TupleExpr arg) {
		assert idx == 0;
		super.setArg(idx, arg);
	}

	/**
	 * Gets the only argument of this n-ary tuple operator.
	 * 
	 * @return The operator's argument.
	 */
	public TupleExpr getArg() {
		return getArg(0);
	}

	/**
	 * Sets the only argument of this n-ary tuple operator.
	 * 
	 * @param arg
	 *        The (new) argument for this operator, must not be <tt>null</tt>.
	 */
	public void setArg(TupleExpr arg) {
		setArg(0, arg);
	}

	@Override
	public Set<String> getBindingNames() {
		return getArg().getBindingNames();
	}

	@Override
	public UnaryTupleOperator clone() {
		return (UnaryTupleOperator)super.clone();
	}
}
