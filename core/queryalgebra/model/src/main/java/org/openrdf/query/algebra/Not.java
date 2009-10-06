/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * A boolean NOT operator operating on a boolean expressions.
 */
public class Not extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Not() {
	}

	public Not(ValueExpr arg) {
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
		return other instanceof Not && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "Not".hashCode();
	}

	@Override
	public Not clone() {
		return (Not)super.clone();
	}
}
