/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;


/**
 * The UNION set operator, which return the union of the result sets of two
 * tuple expressions.
 */
public class Union extends NaryTupleOperator {

	private static final long serialVersionUID = -1894706875682296935L;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Union() {
	}

	/**
	 * Creates a new union operator that operates on the specified arguments.
	 * 
	 * @param args
	 *        The arguments of the union operator.
	 */
	public Union(TupleExpr... args) {
		super(args);
	}

	/**
	 * Creates a new union operator that operates on the specified arguments.
	 * 
	 * @param args
	 *        The arguments of the union operator.
	 */
	public Union(Iterable<? extends TupleExpr> args) {
		super(args);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public Union clone() {
		return (Union)super.clone();
	}
}
