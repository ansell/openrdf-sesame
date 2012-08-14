/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

public class Distinct extends UnaryTupleOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Distinct() {
	}

	public Distinct(TupleExpr arg) {
		super(arg);
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
	public boolean equals(Object other) {
		return other instanceof Distinct && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "Distinct".hashCode();
	}

	@Override
	public Distinct clone() {
		return (Distinct)super.clone();
	}
}
