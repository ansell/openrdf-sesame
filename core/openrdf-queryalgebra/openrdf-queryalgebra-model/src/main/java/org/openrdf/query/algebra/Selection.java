/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.Set;

public class Selection extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueExpr _condition;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Selection() {
	}

	public Selection(TupleExpr arg, ValueExpr condition) {
		super(arg);
		setCondition(condition);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getCondition() {
		return _condition;
	}

	public void setCondition(ValueExpr condition) {
		assert condition != null : "condition must not be null";
		_condition = condition;
		_condition.setParentNode(this);
	}

	public Set<String> getBindingNames() {
		return getArg().getBindingNames();
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
		_condition.visit(visitor);
		super.visitChildren(visitor);
	}

	public String toString() {
		return "SELECT";
	}

	public TupleExpr cloneTupleExpr() {
		TupleExpr arg = getArg().cloneTupleExpr();
		return new Selection(arg, getCondition().cloneValueExpr());
	}
}
