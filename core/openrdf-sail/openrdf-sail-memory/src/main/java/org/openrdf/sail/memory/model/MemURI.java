/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import org.openrdf.model.URI;

/**
 * A MemoryStore-specific implementation of URI that stores separated namespace
 * and local name information to enable reuse of namespace String objects
 * (reducing memory usage) and that gives it node properties.
 */
public class MemURI implements URI, MemResource {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The URI's namespace.
	 */
	private final String _namespace;

	/**
	 * The URI's local name.
	 */
	private final String _localName;

	/**
	 * The object that created this MemURI.
	 */
	private Object _creator;

	/**
	 * The MemURI's hash code, 0 if not yet initialized.
	 */
	private int _hashCode = 0;

	/**
	 * The list of statements for which this MemURI is the subject.
	 */
	private MemStatementList _subjectStatements = null;

	/**
	 * The list of statements for which this MemURI is the predicate.
	 */
	private MemStatementList _predicateStatements = null;

	/**
	 * The list of statements for which this MemURI is the object.
	 */
	private MemStatementList _objectStatements = null;

	/**
	 * The list of statements for which this MemURI represents the context.
	 */
	private MemStatementList _contextStatements = null;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new MemURI for a URI.
	 * 
	 * @param creator
	 *        The object that is creating this MemURI.
	 * @param namespace
	 *        namespace part of URI.
	 * @param localName
	 *        localname part of URI.
	 */
	public MemURI(Object creator, String namespace, String localName) {
		_creator = creator;
		_namespace = namespace;
		_localName = localName;
	}

	/*---------*
	 * Methods *
	 *---------*/

	// Overrides Object.toString(), implements URI.toString()
	public String toString() {
		return _namespace + _localName;
	}

	// Implements URI.getNamespace()
	public String getNamespace() {
		return _namespace;
	}

	// Implements URI.getLocalName()
	public String getLocalName() {
		return _localName;
	}

	// Overrides Object.equals(Object), implements URI.equals(Object)
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}

		if (other instanceof MemURI) {
			MemURI o = (MemURI)other;
			return _namespace.equals(o.getNamespace()) && _localName.equals(o.getLocalName());
		}
		else if (other instanceof URI) {
			return toString().equals(other.toString());
		}

		return false;
	}

	// Overrides Object.hashCode(), implements URI.hashCode()
	public int hashCode() {
		if (_hashCode == 0) {
			_hashCode = toString().hashCode();
		}

		return _hashCode;
	}

	// Implements MemValue.getCreator()
	public Object getCreator() {
		return _creator;
	}

	// Implements MemValue.getSubjectStatementList()
	public MemStatementList getSubjectStatementList() {
		if (_subjectStatements == null) {
			return EMPTY_LIST;
		}
		else {
			return _subjectStatements;
		}
	}

	// Implements MemValue.getSubjectStatementCount()
	public int getSubjectStatementCount() {
		if (_subjectStatements == null) {
			return 0;
		}
		else {
			return _subjectStatements.size();
		}
	}

	// Implements MemValue.addSubjectStatement(MemStatement)
	public void addSubjectStatement(MemStatement st) {
		if (_subjectStatements == null) {
			_subjectStatements = new MemStatementList(4);
		}

		_subjectStatements.add(st);
	}

	// Implements MemValue.removeSubjectStatement(MemStatement)
	public void removeSubjectStatement(MemStatement st) {
		_subjectStatements.remove(st);

		if (_subjectStatements.isEmpty()) {
			_subjectStatements = null;
		}
	}

	/**
	 * Gets the list of statements for which this MemURI is the predicate.
	 * 
	 * @return a MemStatementList containing the statements.
	 */
	public MemStatementList getPredicateStatementList() {
		if (_predicateStatements == null) {
			return EMPTY_LIST;
		}
		else {
			return _predicateStatements;
		}
	}

	/**
	 * Gets the number of Statements for which this MemURI is the predicate.
	 * 
	 * @return An integer larger than or equal to 0.
	 */
	public int getPredicateStatementCount() {
		if (_predicateStatements == null) {
			return 0;
		}
		else {
			return _predicateStatements.size();
		}
	}

	/**
	 * Adds a statement to this MemURI's list of statements for which it is the
	 * predicate.
	 */
	public void addPredicateStatement(MemStatement st) {
		if (_predicateStatements == null) {
			_predicateStatements = new MemStatementList(4);
		}

		_predicateStatements.add(st);
	}

	/**
	 * Removes a statement from this MemURI's list of statements for which it is
	 * the predicate.
	 */
	public void removePredicateStatement(MemStatement st) {
		_predicateStatements.remove(st);

		if (_predicateStatements.isEmpty()) {
			_predicateStatements = null;
		}
	}

	// Implements MemValue.getObjectStatementList()
	public MemStatementList getObjectStatementList() {
		if (_objectStatements == null) {
			return EMPTY_LIST;
		}
		else {
			return _objectStatements;
		}
	}

	// Implements MemValue.getObjectStatementCount()
	public int getObjectStatementCount() {
		if (_objectStatements == null) {
			return 0;
		}
		else {
			return _objectStatements.size();
		}
	}

	// Implements MemValue.addObjectStatement(MemStatement)
	public void addObjectStatement(MemStatement st) {
		if (_objectStatements == null) {
			_objectStatements = new MemStatementList(4);
		}
		_objectStatements.add(st);
	}

	// Implements MemValue.removeObjectStatement(MemStatement)
	public void removeObjectStatement(MemStatement st) {
		_objectStatements.remove(st);
		if (_objectStatements.isEmpty()) {
			_objectStatements = null;
		}
	}

	// Implements MemResource.getContextStatementList()
	public MemStatementList getContextStatementList() {
		if (_contextStatements == null) {
			return EMPTY_LIST;
		}
		else {
			return _contextStatements;
		}
	}

	// Implements MemResource.getContextStatementCount()
	public int getContextStatementCount() {
		if (_contextStatements == null) {
			return 0;
		}
		else {
			return _contextStatements.size();
		}
	}

	// Implements MemResource.addContextStatement(MemStatement)
	public void addContextStatement(MemStatement st) {
		if (_contextStatements == null) {
			_contextStatements = new MemStatementList(4);
		}

		_contextStatements.add(st);
	}

	// Implements MemResource.removeContextStatement(MemStatement)
	public void removeContextStatement(MemStatement st) {
		_contextStatements.remove(st);

		if (_contextStatements.isEmpty()) {
			_contextStatements = null;
		}
	}
}
