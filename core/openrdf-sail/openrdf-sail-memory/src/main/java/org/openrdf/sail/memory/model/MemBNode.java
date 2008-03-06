/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import org.openrdf.model.impl.BNodeImpl;

/**
 * A MemoryStore-specific extension of BNodeImpl giving it node properties.
 */
public class MemBNode extends BNodeImpl implements MemResource {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The object that created this MemBNode.
	 */
	private Object _creator;

	/**
	 * The list of statements for which this MemBNode is the subject.
	 */
	private MemStatementList _subjectStatements;

	/**
	 * The list of statements for which this MemBNode is the object.
	 */
	private MemStatementList _objectStatements;

	/**
	 * The list of statements for which this MemBNode represents the context.
	 */
	private MemStatementList _contextStatements;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new MemBNode for a bnode ID.
	 *
	 * @param creator The object that is creating this MemBNode.
	 * @param id bnode ID.
	 */
	public MemBNode(Object creator, String id) {
		super(id);
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
		if (_subjectStatements == null) {
			return EMPTY_LIST;
		}
		else {
			return _subjectStatements;
		}
	}

	// Implements MemResource.getSubjectStatementCount()
	public int getSubjectStatementCount() {
		if (_subjectStatements == null) {
			return 0;
		}
		else {
			return _subjectStatements.size();
		}
	}

	// Implements MemResource.addSubjectStatement(MemStatement)
	public void addSubjectStatement(MemStatement st) {
		if (_subjectStatements == null) {
			_subjectStatements = new MemStatementList(4);
		}

		_subjectStatements.add(st);
	}

	// Implements MemResource.removeSubjectStatement(MemStatement)
	public void removeSubjectStatement(MemStatement st) {
		_subjectStatements.remove(st);

		if (_subjectStatements.isEmpty()) {
			_subjectStatements = null;
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
