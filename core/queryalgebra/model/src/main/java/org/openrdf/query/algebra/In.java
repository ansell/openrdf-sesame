/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * Checks whether a certain value is contained in a set of results produced by a
 * query.
 */
public class In extends CompareSubQueryValueOperator {

	private static final long serialVersionUID = 3329055265002869613L;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public In() {
	}

	public In(ValueExpr valueExpr, TupleExpr subQuery) {
		super(valueExpr, subQuery);
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
	public In clone() {
		return (In)super.clone();
	}
}
