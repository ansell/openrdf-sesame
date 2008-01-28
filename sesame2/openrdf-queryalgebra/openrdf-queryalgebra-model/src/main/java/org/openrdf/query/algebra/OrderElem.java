/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * @author Arjohn Kampman
 */
public class OrderElem extends QueryModelNodeBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueExpr expr;

	private boolean ascending = true;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public OrderElem() {
	}

	public OrderElem(ValueExpr expr) {
		this(expr, true);
	}

	public OrderElem(ValueExpr expr, boolean ascending) {
		setExpr(expr);
		setAscending(ascending);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getExpr() {
		return expr;
	}

	public void setExpr(ValueExpr expr) {
		assert expr != null : "expr must not be null";
		expr.setParentNode(this);
		this.expr = expr;
	}

	public boolean isAscending() {
		return ascending;
	}

	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		expr.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement)
	{
		if (expr == current) {
			setExpr((ValueExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public String getSignature()
	{
		return super.getSignature() + " (" + (ascending ? "ASC" : "DESC") + ")";
	}

	@Override
	public OrderElem clone() {
		OrderElem clone = (OrderElem)super.clone();
		clone.setExpr(getExpr().clone());
		return clone;
	}
}
