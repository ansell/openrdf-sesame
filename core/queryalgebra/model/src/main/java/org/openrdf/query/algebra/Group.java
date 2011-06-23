/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import info.aduna.collections.iterators.Iterators;

/**
 * A tuple operator that groups tuples that have a specific set of equivalent
 * variable bindings, and that can apply aggregate functions on the grouped
 * results.
 * 
 * @author David Huynh
 * @author Arjohn Kampman
 */
public class Group extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private List<QueryModelNode> groupConditions = new ArrayList<QueryModelNode>();
	
	private List<GroupElem> groupElements = new ArrayList<GroupElem>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Group(TupleExpr arg) {
		super(arg);
	}

	public Group(TupleExpr arg, Iterable<QueryModelNode> groupConditions) {
		this(arg);
		setGroupConditions(groupConditions);
	}

	public Group(TupleExpr arg, Iterable<QueryModelNode> groupConditions, Iterable<GroupElem> groupElements) {
		this(arg, groupConditions);
		setGroupElements(groupElements);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public List<QueryModelNode> getGroupConditions() {
		return Collections.unmodifiableList(groupConditions);
	}
	
	public void setGroupConditions(Iterable<QueryModelNode> groupConditions) {
		this.groupConditions.clear();
		Iterators.addAll(groupConditions.iterator(), this.groupConditions);
	}
	
	public void addGroupCondition(QueryModelNode groupCondition) {
		groupConditions.add(groupCondition);
	}
	
	public List<GroupElem> getGroupElements() {
		return Collections.unmodifiableList(groupElements);
	}

	public void addGroupElement(GroupElem groupElem) {
		groupElements.add(groupElem);
	}

	public void setGroupElements(Iterable<GroupElem> elements) {
		this.groupElements.clear();
		Iterators.addAll(elements.iterator(), this.groupElements);
	}

	public Set<String> getAggregateBindingNames() {
		Set<String> bindings = new HashSet<String>();

		for (GroupElem binding : groupElements) {
			bindings.add(binding.getName());
		}

		return bindings;
	}

	@Override
	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>();

		bindingNames.addAll(getGroupBindingNames());
		bindingNames.addAll(getAggregateBindingNames());

		return bindingNames;
	}

	/**
	 * @return
	 */
	public Collection<? extends String> getGroupBindingNames() {
		
		List<String> groupConditionBindingNames = new ArrayList<String>();
		
		for (QueryModelNode groupCondition: getGroupConditions()) {
			if (groupCondition instanceof Var) {
				groupConditionBindingNames.add(((Var)groupCondition).getName());
			}
			else {
				groupConditionBindingNames.addAll(((Extension)groupCondition).getBindingNames());
			}
		}
		
		return groupConditionBindingNames;
	
	}

	@Override
	public Set<String> getAssuredBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>();

		bindingNames.addAll(getGroupBindingNames());
		bindingNames.retainAll(getArg().getAssuredBindingNames());

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
		super.visitChildren(visitor);

		for (GroupElem ge : groupElements) {
			ge.visit(visitor);
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (replaceNodeInList(groupElements, current, replacement)) {
			return;
		}
		super.replaceChildNode(current, replacement);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Group && super.equals(other)) {
			Group o = (Group)other;
			return groupConditions.equals(o.getGroupConditions())
					&& groupElements.equals(o.getGroupElements());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ groupConditions.hashCode() ^ groupElements.hashCode();
	}

	@Override
	public Group clone() {
		Group clone = (Group)super.clone();


		clone.groupConditions = new ArrayList<QueryModelNode>(getGroupConditions().size());
		for (QueryModelNode groupCondition : getGroupConditions()) {
			clone.addGroupCondition(groupCondition.clone());
		}
		
		clone.groupElements = new ArrayList<GroupElem>(getGroupElements().size());
		for (GroupElem ge : getGroupElements()) {
			clone.addGroupElement(ge.clone());
		}

		return clone;
	}
}
