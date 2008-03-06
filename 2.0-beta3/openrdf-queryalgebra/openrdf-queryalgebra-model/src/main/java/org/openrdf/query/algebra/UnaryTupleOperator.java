/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * An abstract superclass for unary tuple operators which, by definition, has
 * one argument.
 */
public abstract class UnaryTupleOperator extends QueryModelNodeBase implements TupleExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's argument.
	 */
	protected TupleExpr _arg;

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
		setArg(arg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the argument of this unary tuple operator.
	 * 
	 * @return The operator's argument.
	 */
	public TupleExpr getArg() {
		return _arg;
	}

	/**
	 * Sets the argument of this unary tuple operator.
	 * 
	 * @param arg
	 *        The (new) argument for this operator, must not be <tt>null</tt>.
	 */
	public void setArg(TupleExpr arg) {
		assert arg != null : "arg must not be null";
		_arg = arg;
		_arg.setParentNode(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		_arg.visit(visitor);
	}
}
