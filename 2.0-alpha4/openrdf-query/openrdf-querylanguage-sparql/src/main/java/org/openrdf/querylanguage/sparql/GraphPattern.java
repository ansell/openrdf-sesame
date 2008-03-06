/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.sparql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openrdf.querymodel.BooleanExpr;
import org.openrdf.querymodel.Join;
import org.openrdf.querymodel.OptionalJoin;
import org.openrdf.querymodel.RowSelection;
import org.openrdf.querymodel.Selection;
import org.openrdf.querymodel.SingletonSet;
import org.openrdf.querymodel.StatementPattern;
import org.openrdf.querymodel.TupleExpr;
import org.openrdf.querymodel.Var;

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
	private List<BooleanExpr> _constraints = new ArrayList<BooleanExpr>();

	/**
	 * The limit on the number of results this graph pattern should produce.
	 */
	private int _limit = -1;

	/**
	 * The number of results that should be skipped by this graph pattern.
	 */
	private int _offset = -1;

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

	public void addConstraint(BooleanExpr constraint) {
		_constraints.add(constraint);
	}

	public List<BooleanExpr> getConstraints() {
		return Collections.unmodifiableList(_constraints);
	}

	public int getLimit() {
		return _limit;
	}

	public void setLimit(int limit) {
		_limit = limit;
	}

	public int getOffset() {
		return _offset;
	}

	public void setOffset(int offset) {
		_offset = offset;
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

		for (BooleanExpr constraint : _constraints) {
			result = new Selection(result, constraint);
		}

		if (_offset >= 1 || _limit >= 0) {
			result = new RowSelection(result, _offset, _limit);
		}

		return result;
	}
}
