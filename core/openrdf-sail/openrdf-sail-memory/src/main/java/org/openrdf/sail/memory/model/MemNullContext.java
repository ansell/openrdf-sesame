/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

/**
 * A MemResource that can be used as the context for statements that are in the
 * null context.
 */
public class MemNullContext implements MemResource {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The object that created this MemNullContext.
	 */
	private Object _creator;

	/**
	 * The list of statements for which this MemNullContext represents the
	 * context.
	 */
	private MemStatementList _contextStatements;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new MemNullContext.
	 *
	 * @param creator The object that is creating this MemNullContext.
	 */
	public MemNullContext(Object creator) {
		_creator = creator;
	}

	/*---------*
	 * Methods *
	 *---------*/

	// Implements MemValue.getCreator()
    public Object getCreator() {
        return _creator;
    }

	// Implements MemResource.getSubjectStatementList()
	public MemStatementList getSubjectStatementList() {
		return EMPTY_LIST;
	}

	// Implements MemResource.getSubjectStatementCount()
	public int getSubjectStatementCount() {
		return 0;
	}

	// Implements MemResource.addSubjectStatement(MemStatement)
	public void addSubjectStatement(MemStatement st) {
		throw new UnsupportedOperationException(
				"MemNullContext cannot be used as the subject of a statement");
	}

	// Implements MemResource.removeSubjectStatement(MemStatement)
	public void removeSubjectStatement(MemStatement st) {
		// list is always empty, ignore
	}

	// Implements MemValue.getObjectStatementList()
	public MemStatementList getObjectStatementList() {
		return EMPTY_LIST;
	}

	// Implements MemValue.getObjectStatementCount()
	public int getObjectStatementCount() {
		return 0;
	}

	// Implements MemValue.addObjectStatement(MemStatement)
	public void addObjectStatement(MemStatement st) {
		throw new UnsupportedOperationException(
				"MemNullContext cannot be used as the object of a statement");
	}

	// Implements MemValue.removeObjectStatement(MemStatement)
	public void removeObjectStatement(MemStatement st) {
		// list is always empty, ignore
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
