/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A tuple operator that groups tuples that have a specific set of equivalent
 * variable bindings, and that can apply aggregate functions on the grouped
 * results.
 * 
 * @author David Huynh
 * @author Arjohn Kampman
 */
public class GroupElem extends QueryModelNodeBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String name;

	private AggregateOperator operator;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public GroupElem(String name, AggregateOperator operator) {
		setName(name);
		setOperator(operator);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getName() {
		return name;
	}

	public void setName(String name) {
		assert name != null : "name must not be null";
		this.name = name;
	}

	public AggregateOperator getOperator() {
		return operator;
	}

	public void setOperator(AggregateOperator operator) {
		assert operator != null : "operator must not be null";
		this.operator = operator;
		operator.setParentNode(this);
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
		operator.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement)
	{
		if (operator == current) {
			setOperator((AggregateOperator)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	public GroupElem clone() {
		GroupElem clone = (GroupElem)super.clone();
		clone.setOperator(getOperator().clone());
		return clone;
	}
}
