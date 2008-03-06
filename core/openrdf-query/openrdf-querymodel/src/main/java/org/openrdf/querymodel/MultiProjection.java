/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;

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
	private List<Projection> _projections;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MultiProjection(TupleExpr arg) {
		super(arg);
		_projections = new ArrayList<Projection>();
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

	public void add(Projection projection) {
		_projections.add(projection);
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

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	@Override
	public void visitChildren(QueryModelVisitor visitor) {
		for (Projection p : _projections) {
			p.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	public String toString() {
		return "MULTIPROJECT";
	}
}
