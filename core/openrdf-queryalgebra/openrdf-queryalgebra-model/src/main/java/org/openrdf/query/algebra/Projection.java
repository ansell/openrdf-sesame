/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A generalized projection (allowing the bindings to be renamed) on a tuple
 * expression.
 */
public class Projection extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private List<ProjectionElem> _elements = new ArrayList<ProjectionElem>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Projection() {
	}

	public Projection(TupleExpr arg) {
		super(arg);
	}

	public Projection(TupleExpr arg, ProjectionElem... elements) {
		this(arg);
		add(elements);
	}

	public Projection(TupleExpr arg, Iterable<ProjectionElem> elements) {
		this(arg);
		add(elements);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public List<ProjectionElem> getElements() {
		return _elements;
	}

	public void setElements(List<ProjectionElem> elements) {
		_elements = elements;
	}

	public void add(ProjectionElem pe) {
		assert pe != null : "pe must not be null";
		_elements.add(pe);
		pe.setParentNode(this);
	}

	public void add(ProjectionElem... elements) {
		for (ProjectionElem pe : elements) {
			add(pe);
		}
	}

	public void add(Iterable<ProjectionElem> elements) {
		for (ProjectionElem pe : elements) {
			add(pe);
		}
	}

	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(2 * _elements.size());

		for (ProjectionElem pe : _elements) {
			bindingNames.add(pe.getTargetName());
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
		for (ProjectionElem pe : _elements) {
			pe.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	public String toString() {
		return "PROJECT";
	}

	public TupleExpr cloneTupleExpr() {
		return cloneProjection();
	}

	public Projection cloneProjection() {
		TupleExpr arg = getArg().cloneTupleExpr();
		List<ProjectionElem> elements = new ArrayList<ProjectionElem>(_elements.size());
		for (ProjectionElem pe : _elements) {
			elements.add(pe.cloneProjectionElem());
		}
		return new Projection(arg, elements);
	}
}
