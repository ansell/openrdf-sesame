/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

public abstract class SubQueryValueOperator extends QueryModelNodeBase implements ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	protected TupleExpr _subQuery;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SubQueryValueOperator() {
	}

	public SubQueryValueOperator(TupleExpr subQuery) {
		setSubQuery(subQuery);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public TupleExpr getSubQuery() {
		return _subQuery;
	}

	public void setSubQuery(TupleExpr subQuery) {
		assert subQuery != null : "subQuery must not be null";
		_subQuery = subQuery;
		subQuery.setParentNode(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		_subQuery.visit(visitor);
	}

}
