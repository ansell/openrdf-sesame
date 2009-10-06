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

	protected TupleExpr subQuery;

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
		return subQuery;
	}

	public void setSubQuery(TupleExpr subQuery) {
		assert subQuery != null : "subQuery must not be null";
		subQuery.setParentNode(this);
		this.subQuery = subQuery;
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		subQuery.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (subQuery == current) {
			setSubQuery((TupleExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof SubQueryValueOperator) {
			SubQueryValueOperator o = (SubQueryValueOperator)other;
			return subQuery.equals(o.getSubQuery());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return subQuery.hashCode();
	}

	@Override
	public SubQueryValueOperator clone() {
		SubQueryValueOperator clone = (SubQueryValueOperator)super.clone();
		clone.setSubQuery(getSubQuery().clone());
		return clone;
	}
}
