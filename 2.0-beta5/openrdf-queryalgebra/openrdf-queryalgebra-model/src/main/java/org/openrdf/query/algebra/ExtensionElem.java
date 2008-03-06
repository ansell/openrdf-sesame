/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

public class ExtensionElem extends QueryModelNodeBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueExpr _expr;

	private String _name;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ExtensionElem() {
	}

	public ExtensionElem(ValueExpr expr, String name) {
		setArg(expr);
		setName(name);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getArg() {
		return _expr;
	}

	public void setArg(ValueExpr expr) {
		assert expr != null : "expr must not be null";
		_expr = expr;
		_expr.setParentNode(this);
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
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
		_expr.visit(visitor);
	}

	public String toString() {
		return "EXT_ELEM (" + _name + ")";
	}

	public ExtensionElem cloneExtensionElem() {
		return new ExtensionElem(getArg().cloneValueExpr(), getName());
	}
}
