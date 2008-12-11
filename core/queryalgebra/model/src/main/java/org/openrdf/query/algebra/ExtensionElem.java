/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

public class ExtensionElem extends QueryModelNodeBase {

	private static final long serialVersionUID = 9219499840115634313L;

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueExpr expr;

	private String name;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ExtensionElem() {
	}

	public ExtensionElem(ValueExpr expr, String name) {
		setExpr(expr);
		setName(name);
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		return super.getSignature() + " (" + name + ")";
	}

	@Override
	public ExtensionElem clone() {
		ExtensionElem clone = (ExtensionElem)super.clone();
		clone.setExpr(getExpr().clone());
		return clone;
	}
}
