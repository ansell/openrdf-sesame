/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The LeftJoin operator, as defined in <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#algLeftJoin">SPARQL Query
 * Language for RDF</a>.
 * 
 * @author Arjohn Kampman
 */
public class LeftJoin extends BinaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueExpr condition;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public LeftJoin() {
	}

	public LeftJoin(TupleExpr leftArg, TupleExpr rightArg) {
		super(leftArg, rightArg);
	}

	public LeftJoin(TupleExpr leftArg, TupleExpr rightArg, ValueExpr condition) {
		this(leftArg, rightArg);
		setCondition(condition);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getCondition() {
		return condition;
	}

	public void setCondition(ValueExpr condition) {
		if (condition != null) {
			condition.setParentNode(this);
		}
		this.condition = condition;
	}

	public boolean hasCondition() {
		return condition != null;
	}

	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(16);
		bindingNames.addAll(getLeftArg().getBindingNames());
		bindingNames.addAll(getRightArg().getBindingNames());
		return bindingNames;
	}

	public Set<String> getAssuredBindingNames() {
		return getLeftArg().getAssuredBindingNames();
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
		if (condition != null) {
			condition.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (condition == current) {
			setCondition((ValueExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof LeftJoin && super.equals(other)) {
			ValueExpr oCond = ((LeftJoin)other).getCondition();
			return nullEquals(condition, oCond);
		}

		return false;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode() ^ "LeftJoin".hashCode();
		if (hasCondition()) {
			result ^= getCondition().hashCode();
		}
		return result;
	}

	@Override
	public LeftJoin clone() {
		LeftJoin clone = (LeftJoin)super.clone();
		if (hasCondition()) {
			clone.setCondition(getCondition().clone());
		}
		return clone;
	}
}
