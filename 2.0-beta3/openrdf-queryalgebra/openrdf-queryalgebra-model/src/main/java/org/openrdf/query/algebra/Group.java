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
public class Group extends UnaryTupleOperator {

	/*------------*
	 * Structures *
	 *------------*/

	static public class AggregateBinding {

		final private String _name;

		final private AggregateOperator _operator;

		public AggregateBinding(String name, AggregateOperator operator) {
			_name = name;
			_operator = operator;
		}

		public String getName() {
			return _name;
		}

		public AggregateOperator getOperator() {
			return _operator;
		}
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private Set<String> _groupBindings = new LinkedHashSet<String>();

	private Set<AggregateBinding> _aggregateBindings = new LinkedHashSet<AggregateBinding>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Group(TupleExpr tupleExpr, String[] groupBindingNames, AggregateBinding[] aggregateBindings) {
		super(tupleExpr);

		for (String name : groupBindingNames) {
			_groupBindings.add(name);
		}
		for (AggregateBinding binding : aggregateBindings) {
			_aggregateBindings.add(binding);
		}
	}

	public Group(TupleExpr tupleExpr, Iterable<String> groupBindingNames,
			Iterable<AggregateBinding> aggregateBindings)
	{
		super(tupleExpr);

		for (String name : groupBindingNames) {
			_groupBindings.add(name);
		}
		for (AggregateBinding binding : aggregateBindings) {
			_aggregateBindings.add(binding);
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>();

		bindingNames.addAll(getGroupBindingNames());
		bindingNames.addAll(getAggregateBindingNames());

		return bindingNames;
	}

	public Set<String> getGroupBindingNames() {
		return Collections.unmodifiableSet(_groupBindings);
	}

	public Set<String> getAggregateBindingNames() {
		Set<String> bindings = new HashSet<String>();

		for (AggregateBinding binding : _aggregateBindings) {
			bindings.add(binding.getName());
		}

		return bindings;
	}

	public Set<AggregateBinding> getAggregateBindings() {
		return Collections.unmodifiableSet(_aggregateBindings);
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

		for (AggregateBinding binding : _aggregateBindings) {
			binding.getOperator().visit(visitor);
		}
	}

	public String toString() {
		return "GROUP";
	}

	public TupleExpr cloneTupleExpr() {
		TupleExpr arg = getArg().cloneTupleExpr();
		return new Group(arg, _groupBindings, _aggregateBindings);
	}
}
