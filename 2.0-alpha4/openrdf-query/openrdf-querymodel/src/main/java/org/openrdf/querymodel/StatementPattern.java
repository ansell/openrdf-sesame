/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;

import java.util.HashSet;
import java.util.Set;

/**
 * A tuple expression that matches a statement pattern against an RDF graph.
 * Statement patterns can be targeted at one of three context scopes: all
 * contexts, null context only, or named contexts only.
 */
public class StatementPattern extends TupleExpr {

	/*------------*
	 * enum Scope *
	 *------------*/

	/**
	 * Indicates the scope of the statement pattern.
	 */
	public enum Scope {
		/**
		 * Scope for patterns that should be matched against statements from all
		 * contexts, named and null.
		 */
		ALL_CONTEXTS,

		/**
		 * Scope for patterns that should be matched against statements from the
		 * null context only.
		 */
		NULL_CONTEXT,

		/**
		 * Scope for patterns that should be matched against statements from named
		 * contexts only.
		 */
		NAMED_CONTEXTS
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private Scope _scope;

	private Var _subjectVar;

	private Var _predicateVar;

	private Var _objectVar;

	private Var _contextVar;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a statement pattern that matches a subject-, predicate- and object
	 * variable against statements from all contexts.
	 */
	public StatementPattern(Var subject, Var predicate, Var object) {
		this(Scope.ALL_CONTEXTS, subject, predicate, object);
	}

	/**
	 * Creates a statement pattern that matches a subject-, predicate- and object
	 * variable against statements from the specified context scope.
	 */
	public StatementPattern(Scope scope, Var subject, Var predicate, Var object) {
		this(scope, subject, predicate, object, null);
	}

	/**
	 * Creates a statement pattern that matches a subject-, predicate-, object-
	 * and context variable against statements from all contexts.
	 */
	public StatementPattern(Var subject, Var predicate, Var object, Var context) {
		this(Scope.ALL_CONTEXTS, subject, predicate, object, context);
	}

	/**
	 * Creates a statement pattern that matches a subject-, predicate-, object-
	 * and context variable against statements from the specified context scope.
	 */
	public StatementPattern(Scope scope, Var subjVar, Var predVar, Var objVar, Var conVar) {
		assert scope != null : "scope must not be null";
		assert subjVar != null : "subjVar must not be null";
		assert predVar != null : "predVar must not be null";
		assert objVar != null : "objVar must not be null";

		_scope = scope;
		_subjectVar = subjVar;
		_predicateVar = predVar;
		_objectVar = objVar;
		_contextVar = conVar;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the context scope for the statement pattern.
	 */
	public Scope getScope() {
		return _scope;
	}

	/**
	 * Sets the context scope for the statement pattern.
	 */
	public void setScope(Scope scope) {
		_scope = scope;
	}

	public Var getSubjectVar() {
		return _subjectVar;
	}

	public void setSubjectVar(Var subject) {
		_subjectVar = subject;
	}

	public Var getPredicateVar() {
		return _predicateVar;
	}

	public void setPredicateVar(Var predicate) {
		_predicateVar = predicate;
	}

	public Var getObjectVar() {
		return _objectVar;
	}

	public void setObjectVar(Var object) {
		_objectVar = object;
	}

	/**
	 * Returns the context variable, if available.
	 * @return
	 */
	public Var getContextVar() {
		return _contextVar;
	}

	public void setContextVar(Var context) {
		_contextVar = context;
	}

	public Set<String> getBindingNames() {
		Set<String> bindingNames = new HashSet<String>(8);

		if (_subjectVar != null && !_subjectVar.hasValue()) {
			bindingNames.add(_subjectVar.getName());
		}
		if (_predicateVar != null && !_predicateVar.hasValue()) {
			bindingNames.add(_predicateVar.getName());
		}
		if (_objectVar != null && !_objectVar.hasValue()) {
			bindingNames.add(_objectVar.getName());
		}
		if (_contextVar != null && !_contextVar.hasValue()) {
			bindingNames.add(_contextVar.getName());
		}

		return bindingNames;
	}

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	@Override
	public void visitChildren(QueryModelVisitor visitor) {
		if (_subjectVar != null) {
			_subjectVar.visit(visitor);
		}
		if (_predicateVar != null) {
			_predicateVar.visit(visitor);
		}
		if (_objectVar != null) {
			_objectVar.visit(visitor);
		}
		if (_contextVar != null) {
			_contextVar.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(128);

		sb.append("Statements");

		switch (_scope) {
			case NULL_CONTEXT:
				sb.append(" FROM NULL CONTEXT");
				break;
			case NAMED_CONTEXTS:
				sb.append(" FROM NAMED CONTEXT");
				break;
		}

		return sb.toString();
	}
}
