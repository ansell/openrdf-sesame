/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
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
	private List<Projection> _projections = new ArrayList<Projection>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MultiProjection() {
	}

	public MultiProjection(TupleExpr arg) {
		super(arg);
	}

	public MultiProjection(TupleExpr arg, Iterable<Projection> projections) {
		this(arg);
		add(projections);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public List<Projection> getProjections() {
		return Collections.unmodifiableList(_projections);
	}

	public void setProjections(List<Projection> projections) {
		_projections = projections;
	}

	public void add(Projection projection) {
		assert projection != null : "projection must not be null";
		_projections.add(projection);
		projection.setParentNode(this);
	}

	public void add(Projection... projections) {
		for (Projection p : projections) {
			add(p);
		}
	}

	public void add(Iterable<Projection> projections) {
		for (Projection p : projections) {
			add(p);
		}
	}

	public Set<String> getBindingNames() {
		Set<String> bindingNames = new HashSet<String>();

		for (Projection projection : _projections) {
			bindingNames.addAll(projection.getBindingNames());
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
		for (Projection p : _projections) {
			p.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	public String toString() {
		return "MULTIPROJECT";
	}

	public TupleExpr cloneTupleExpr() {
		TupleExpr arg = getArg().cloneTupleExpr();
		List<Projection> projections = new ArrayList<Projection>(_projections.size());
		for (Projection p : _projections) {
			projections.add(p.cloneProjection());
		}
		return new MultiProjection(arg, projections);
	}
}
