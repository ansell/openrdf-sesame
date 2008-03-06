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
	 * 
	 */
	private static final long serialVersionUID = -887382892580321647L;

	/**
	 * The object that created this MemBNode.
	 */
	transient private Object creator;

	/**
	 * The list of statements for which this MemBNode is the subject.
	 */
	transient private MemStatementList subjectStatements;

	/**
	 * The list of statements for which this MemBNode is the object.
	 */
	transient private MemStatementList objectStatements;

	/**
	 * The list of statements for which this MemBNode represents the context.
	 */
	transient private MemStatementList contextStatements;

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
		this.creator = creator;
	}

	/*---------*
	 * Methods *
	 *---------*/

	// Implements MemValue.getCreator()
    public Object getCreator() {
        return creator;
    }

	// Implements MemResource.getSubjectStatementList()
	public MemStatementList getSubjectStatementList() {
		if (subjectStatements == null) {
			return EMPTY_LIST;
		}
		else {
			return subjectStatements;
		}
	}

	// Implements MemResource.getSubjectStatementCount()
	public int getSubjectStatementCount() {
		if (subjectStatements == null) {
			return 0;
		}
		else {
			return subjectStatements.size();
		}
	}

	// Implements MemResource.addSubjectStatement(MemStatement)
	public void addSubjectStatement(MemStatement st) {
		if (subjectStatements == null) {
			subjectStatements = new MemStatementList(4);
		}

		subjectStatements.add(st);
	}

	// Implements MemResource.removeSubjectStatement(MemStatement)
	public void removeSubjectStatement(MemStatement st) {
		subjectStatements.remove(st);

		if (subjectStatements.isEmpty()) {
			subjectStatements = null;
		}
	}

	// Implements MemValue.getObjectStatementList()
	public MemStatementList getObjectStatementList() {
		if (objectStatements == null) {
			return EMPTY_LIST;
		}
		else {
			return objectStatements;
		}
	}

	// Implements MemValue.getObjectStatementCount()
	public int getObjectStatementCount() {
		if (objectStatements == null) {
			return 0;
		}
		else {
			return objectStatements.size();
		}
	}

	// Implements MemValue.addObjectStatement(MemStatement)
	public void addObjectStatement(MemStatement st) {
		if (objectStatements == null) {
			objectStatements = new MemStatementList(4);
		}

		objectStatements.add(st);
	}

	// Implements MemValue.removeObjectStatement(MemStatement)
	public void removeObjectStatement(MemStatement st) {
		objectStatements.remove(st);

		if (objectStatements.isEmpty()) {
			objectStatements = null;
		}
	}

	// Implements MemResource.getContextStatementList()
	public MemStatementList getContextStatementList() {
		if (contextStatements == null) {
			return EMPTY_LIST;
		}
		else {
			return contextStatements;
		}
	}

	// Implements MemResource.getContextStatementCount()
	public int getContextStatementCount() {
		if (contextStatements == null) {
			return 0;
		}
		else {
			return contextStatements.size();
		}
	}

	// Implements MemResource.addContextStatement(MemStatement)
	public void addContextStatement(MemStatement st) {
		if (contextStatements == null) {
			contextStatements = new MemStatementList(4);
		}

		contextStatements.add(st);
	}

	// Implements MemResource.removeContextStatement(MemStatement)
	public void removeContextStatement(MemStatement st) {
		contextStatements.remove(st);

		if (contextStatements.isEmpty()) {
			contextStatements = null;
		}
	}
}
