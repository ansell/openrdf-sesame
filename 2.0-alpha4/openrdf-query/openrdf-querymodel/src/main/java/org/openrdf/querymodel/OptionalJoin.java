/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;

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

	private BooleanExpr _condition;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public OptionalJoin(TupleExpr leftArg, TupleExpr rightArg) {
		super(leftArg, rightArg);
	}

	public OptionalJoin(TupleExpr leftArg, TupleExpr rightArg, BooleanExpr condition) {
		this(leftArg, rightArg);
		setCondition(condition);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public BooleanExpr getCondition() {
		return _condition;
	}

	public void setCondition(BooleanExpr condition) {
		_condition = condition;
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

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	@Override
	public void visitChildren(QueryModelVisitor visitor)
	{
		if (_condition != null) {
			_condition.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	public String toString() {
		return "OPTIONAL JOIN";
	}
}
