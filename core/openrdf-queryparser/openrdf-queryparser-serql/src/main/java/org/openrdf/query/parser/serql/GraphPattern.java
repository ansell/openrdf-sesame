/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.OptionalJoin;
import org.openrdf.query.algebra.RowSelection;
import org.openrdf.query.algebra.Selection;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;

/**
 * A graph pattern consisting of (required and optional) tuple expressions and
 * boolean constraints.
 */
class GraphPattern {

	/**
	 * The graph pattern's parent, if any.
	 */
	private GraphPattern parent = null;

	/**
	 * The context of this graph pattern.
	 */
	private Var contextVar;

	/**
	 * The StatementPattern-scope of this graph pattern.
	 */
	private StatementPattern.Scope spScope = StatementPattern.Scope.ALL_CONTEXTS;

	/**
	 * The required tuple expressions in this graph pattern.
	 */
	private List<TupleExpr> requiredTEs = new ArrayList<TupleExpr>();

	/**
	 * The optional tuple expressions in this graph pattern.
	 */
	private List<TupleExpr> optionalTEs = new ArrayList<TupleExpr>();

	/**
	 * The boolean constraints in this graph pattern.
	 */
	private List<ValueExpr> constraints = new ArrayList<ValueExpr>();

	/**
	 * The limit on the number of results this graph pattern should produce.
	 */
	private int limit = -1;

	/**
	 * The number of results that should be skipped by this graph pattern.
	 */
	private int offset = -1;

	/**
	 * Creates a new graph pattern.
	 */
	public GraphPattern() {
	}

	/**
	 * Creates a new graph pattern that inherits the context and scope from a
	 * parent graph pattern.
	 */
	public GraphPattern(GraphPattern parent) {
		if (parent != null) {
			this.parent = parent;
			contextVar = parent.contextVar;
			spScope = parent.spScope;
		}
	}

	public GraphPattern getParent() {
		return parent;
	}

	public void setContextVar(Var contextVar) {
		this.contextVar = contextVar;
	}

	public Var getContextVar() {
		return contextVar;
	}

	public void setStatementPatternScope(StatementPattern.Scope spScope) {
		this.spScope = spScope;
	}

	public StatementPattern.Scope getStatementPatternScope() {
		return spScope;
	}

	public void addRequiredTE(TupleExpr te) {
		requiredTEs.add(te);
	}

	public List<TupleExpr> getRequiredTEs() {
		return Collections.unmodifiableList(requiredTEs);
	}

	public void addOptionalTE(TupleExpr te) {
		optionalTEs.add(te);
	}

	public List<TupleExpr> getOptionalTEs() {
		return Collections.unmodifiableList(optionalTEs);
	}

	public void addConstraint(ValueExpr constraint) {
		constraints.add(constraint);
	}

	public List<ValueExpr> getConstraints() {
		return Collections.unmodifiableList(constraints);
	}

	/**
	 * Builds a combined tuple expression from the tuple expressions and
	 * constraints in this graph pattern.
	 * 
	 * @return A tuple expression for this graph pattern.
	 */
	public TupleExpr buildTupleExpr() {
		TupleExpr result;

		if (requiredTEs.isEmpty()) {
			result = new SingletonSet();
		}
		else {
			result = requiredTEs.get(0);

			for (int i = 1; i < requiredTEs.size(); i++) {
				result = new Join(result, requiredTEs.get(i));
			}
		}

		for (TupleExpr optTE : optionalTEs) {
			result = new OptionalJoin(result, optTE);
		}

		for (ValueExpr constraint : constraints) {
			result = new Selection(result, constraint);
		}
		return result;
	}
}
