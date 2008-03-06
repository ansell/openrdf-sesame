/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An optional join between two tuple expressions, the right argument being the
 * optional part.
 */
public class OptionalJoin extends BinaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueExpr _condition;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public OptionalJoin() {
	}

	public OptionalJoin(TupleExpr leftArg, TupleExpr rightArg) {
		super(leftArg, rightArg);
	}

	public OptionalJoin(TupleExpr leftArg, TupleExpr rightArg, ValueExpr condition) {
		this(leftArg, rightArg);
		setCondition(condition);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getCondition() {
		return _condition;
	}

	public void setCondition(ValueExpr condition) {
		_condition = condition;
		if (condition != null) {
			_condition.setParentNode(this);
		}
	}

	public boolean hasCondition() {
		return _condition != null;
	}

	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(16);
		bindingNames.addAll(getLeftArg().getBindingNames());
		bindingNames.addAll(getRightArg().getBindingNames());
		return bindingNames;
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
		if (_condition != null) {
			_condition.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	public String toString() {
		return "OPTIONAL JOIN";
	}

	public TupleExpr cloneTupleExpr() {
		TupleExpr leftArg = getLeftArg().cloneTupleExpr();
		TupleExpr rightArg = getRightArg().cloneTupleExpr();
		ValueExpr condition = null;
		if (_condition != null)
			condition = _condition.cloneValueExpr();
		return new OptionalJoin(leftArg, rightArg, condition);
	}
}
