/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.Set;

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

	public Set<String> getBindingNames() {
		return getArg().getBindingNames();
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public String toString() {
		return "DISTINCT";
	}

	public TupleExpr cloneTupleExpr() {
		return new Distinct(getArg().cloneTupleExpr());
	}
}
