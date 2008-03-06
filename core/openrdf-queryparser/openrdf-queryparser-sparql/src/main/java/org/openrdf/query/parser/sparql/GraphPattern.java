/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

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
 * 
 * @author arjohn
 */
class GraphPattern {

	/**
	 * The context of this graph pattern.
	 */
	private Var _contextVar;

	/**
	 * The StatementPattern-scope of this graph pattern.
	 */
	private StatementPattern.Scope _spScope = StatementPattern.Scope.ALL_CONTEXTS;

	/**
	 * The required tuple expressions in this graph pattern.
	 */
	private List<TupleExpr> _requiredTEs = new ArrayList<TupleExpr>();

	/**
	 * The optional tuple expressions in this graph pattern.
	 */
	private List<TupleExpr> _optionalTEs = new ArrayList<TupleExpr>();

	/**
	 * The boolean constraints in this graph pattern.
	 */
	private List<ValueExpr> _constraints = new ArrayList<ValueExpr>();

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
		_contextVar = parent._contextVar;
		_spScope = parent._spScope;
	}

	public void setContextVar(Var contextVar) {
		_contextVar = contextVar;
	}

	public Var getContextVar() {
		return _contextVar;
	}

	public void setStatementPatternScope(StatementPattern.Scope spScope) {
		_spScope = spScope;
	}

	public StatementPattern.Scope getStatementPatternScope() {
		return _spScope;
	}

	public void addRequiredTE(TupleExpr te) {
		_requiredTEs.add(te);
	}

	public void addRequiredSP(Var subjVar, Var predVar, Var objVar) {
		addRequiredTE(new StatementPattern(_spScope, subjVar, predVar, objVar, _contextVar));
	}

	public List<TupleExpr> getRequiredTEs() {
		return Collections.unmodifiableList(_requiredTEs);
	}

	public void addOptionalTE(TupleExpr te) {
		_optionalTEs.add(te);
	}

	public List<TupleExpr> getOptionalTEs() {
		return Collections.unmodifiableList(_optionalTEs);
	}

	public void addConstraint(ValueExpr constraint) {
		_constraints.add(constraint);
	}

	public List<ValueExpr> getConstraints() {
		return Collections.unmodifiableList(_constraints);
	}

	/**
	 * Builds a combined tuple expression from the tuple expressions and
	 * constraints in this graph pattern.
	 * 
	 * @return A tuple expression for this graph pattern.
	 */
	public TupleExpr buildTupleExpr() {
		TupleExpr result;

		if (_requiredTEs.isEmpty()) {
			result = new SingletonSet();
		}
		else {
			result = _requiredTEs.get(0);

			for (int i = 1; i < _requiredTEs.size(); i++) {
				result = new Join(result, _requiredTEs.get(i));
			}
		}

		for (TupleExpr optTE : _optionalTEs) {
			result = new OptionalJoin(result, optTE);
		}

		for (ValueExpr constraint : _constraints) {
			result = new Selection(result, constraint);
		}
		
		return result;
	}
}
