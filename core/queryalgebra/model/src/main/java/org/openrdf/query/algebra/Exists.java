/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * Checks whether the wrapped Query produces any results.
 */
public class Exists extends SubQueryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Exists() {
	}

	public Exists(TupleExpr subQuery) {
		super(subQuery);
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
		return other instanceof Exists && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "Exists".hashCode();
	}

	@Override
	public Exists clone() {
		return (Exists)super.clone();
	}
}
