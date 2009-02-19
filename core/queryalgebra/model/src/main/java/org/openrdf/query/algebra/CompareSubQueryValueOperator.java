/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

public abstract class CompareSubQueryValueOperator extends SubQueryValueOperator {

	private static final long serialVersionUID = -3272603083689331081L;

	/*-----------*
	 * Variables *
	 *-----------*/

	protected ValueExpr arg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public CompareSubQueryValueOperator() {
	}

	public CompareSubQueryValueOperator(ValueExpr valueExpr, TupleExpr subQuery) {
		super(subQuery);
		setArg(valueExpr);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getArg() {
		return arg;
	}

	public void setArg(ValueExpr arg) {
		assert arg != null : "arg must not be null";
		arg.setParentNode(this);
		this.arg = arg;
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		arg.visit(visitor);
		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement)
	{
		if (arg == current) {
			setArg((ValueExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public CompareSubQueryValueOperator clone() {
		CompareSubQueryValueOperator clone = (CompareSubQueryValueOperator)super.clone();
		clone.setArg(getArg().clone());
		return clone;
	}
}
