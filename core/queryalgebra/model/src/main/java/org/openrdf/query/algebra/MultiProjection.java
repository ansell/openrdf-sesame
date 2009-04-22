/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A "multi-projection" that can produce multiple solutions from a single set of
 * bindings.
 */
public class MultiProjection extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The lists of projections.
	 */
	private List<ProjectionElemList> projections = new ArrayList<ProjectionElemList>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MultiProjection() {
	}

	public MultiProjection(TupleExpr arg) {
		super(arg);
	}

	public MultiProjection(TupleExpr arg, Iterable<ProjectionElemList> projections) {
		this(arg);
		addProjections(projections);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public List<ProjectionElemList> getProjections() {
		return Collections.unmodifiableList(projections);
	}

	public void setProjections(Iterable<ProjectionElemList> projections) {
		this.projections.clear();
		addProjections(projections);
	}

	public void addProjections(Iterable<ProjectionElemList> projections) {
		for (ProjectionElemList projection : projections) {
			addProjection(projection);
		}
	}

	public void addProjection(ProjectionElemList projection) {
		assert projection != null : "projection must not be null";
		projections.add(projection);
		projection.setParentNode(this);
	}

	@Override
	public Set<String> getBindingNames() {
		Set<String> bindingNames = new HashSet<String>();

		for (ProjectionElemList projElemList : projections) {
			bindingNames.addAll(projElemList.getTargetNames());
		}

		return bindingNames;
	}

	@Override
	public Set<String> getAssuredBindingNames() {
		Set<String> bindingNames = new HashSet<String>();

		if (projections.size() >= 1) {
			Set<String> assuredSourceNames = getArg().getAssuredBindingNames();

			bindingNames.addAll(projections.get(0).getTargetNamesFor(assuredSourceNames));

			for (int i = 1; i < projections.size(); i++) {
				bindingNames.retainAll(projections.get(i).getTargetNamesFor(assuredSourceNames));
			}
		}

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
		for (ProjectionElemList projElemList : projections) {
			projElemList.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		int index = projections.indexOf(current);
		if (index >= 0) {
			projections.set(index, (ProjectionElemList)replacement);
			replacement.setParentNode(this);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public MultiProjection clone() {
		MultiProjection clone = (MultiProjection)super.clone();

		clone.projections = new ArrayList<ProjectionElemList>(getProjections().size());
		for (ProjectionElemList pe : getProjections()) {
			clone.addProjection(pe.clone());
		}

		return clone;
	}
}
