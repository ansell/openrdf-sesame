/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;

import java.util.Set;

public class Selection extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private BooleanExpr _condition;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Selection(TupleExpr arg, BooleanExpr condition) {
		super(arg);
		setCondition(condition);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public BooleanExpr getCondition() {
		return _condition;
	}

	public void setCondition(BooleanExpr condition) {
		assert condition != null : "condition must not be null";
		_condition = condition;
	}

	public Set<String> getBindingNames() {
		return getArg().getBindingNames();
	}

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	@Override
	public void visitChildren(QueryModelVisitor visitor) {
		_condition.visit(visitor);
		super.visitChildren(visitor);
	}

	public String toString() {
		return "SELECT";
	}
}
