/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;

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

	private List<ProjectionElem> _elements;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Projection(TupleExpr arg) {
		super(arg);
		_elements = new ArrayList<ProjectionElem>();
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

	public void add(ProjectionElem pe) {
		_elements.add(pe);
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

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	@Override
	public void visitChildren(QueryModelVisitor visitor) {
		for (ProjectionElem pe : _elements) {
			pe.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	public String toString() {
		return "PROJECT";
	}
}
