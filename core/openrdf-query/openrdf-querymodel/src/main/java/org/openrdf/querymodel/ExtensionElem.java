/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;

public class ExtensionElem extends QueryModelNode {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueExpr _expr;

	private String _name;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ExtensionElem(ValueExpr expr, String name) {
		setExpr(expr);
		setName(name);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getExpr() {
		return _expr;
	}

	public void setExpr(ValueExpr expr) {
		_expr = expr;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	@Override
	public void visitChildren(QueryModelVisitor visitor) {
		_expr.visit(visitor);
	}

	public String toString() {
		return "EXT_ELEM (" + _name + ")";
	}
}
