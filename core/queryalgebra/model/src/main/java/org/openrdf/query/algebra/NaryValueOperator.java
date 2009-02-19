/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.Collection;

/**
 * An abstract superclass for value operators which have one or more
 * arguments.
 */
public abstract class NaryValueOperator extends NaryOperator<ValueExpr> implements ValueExpr {

	private static final long serialVersionUID = -3331959115645639035L;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NaryValueOperator() {
	}

	/**
	 * Creates a new nary tuple operator.
	 * 
	 * @param args
	 *        The operator's arguments, must not be <tt>null</tt>.
	 */
	public NaryValueOperator(ValueExpr... args) {
		super(args);
	}

	/**
	 * Creates a new nary tuple operator.
	 * 
	 * @param args
	 *        The operator's arguments, must not be <tt>null</tt>.
	 */
	public NaryValueOperator(Collection<? extends ValueExpr> args) {
		super(args);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public NaryValueOperator clone() {
		return (NaryValueOperator)super.clone();
	}
}
