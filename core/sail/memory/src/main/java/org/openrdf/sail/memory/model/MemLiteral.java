/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;

/**
 * A MemoryStore-specific extension of Literal giving it node properties.
 * 
 * @author Arjohn Kampman
 */
public class MemLiteral extends LiteralImpl implements MemValue {
	
	private static final long serialVersionUID = 4288477328829845024L;

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The object that created this MemLiteral.
	 */
	transient private final Object creator;

	/**
	 * The list of statements for which this MemLiteral is the object.
	 */
	transient private volatile MemStatementList objectStatements;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new Literal which will get the supplied label.
	 * 
	 * @param creator
	 *        The object that is creating this MemLiteral.
	 * @param label
	 *        The label for this literal.
	 */
	public MemLiteral(Object creator, String label) {
		super(label);
		this.creator = creator;
	}

	/**
	 * Creates a new Literal which will get the supplied label and language code.
	 * 
	 * @param creator
	 *        The object that is creating this MemLiteral.
	 * @param label
	 *        The label for this literal.
	 * @param lang
	 *        The language code of the supplied label.
	 */
	public MemLiteral(Object creator, String label, String lang) {
		super(label, lang);
		this.creator = creator;
	}

	/**
	 * Creates a new Literal which will get the supplied label and datatype.
	 * 
	 * @param creator
	 *        The object that is creating this MemLiteral.
	 * @param label
	 *        The label for this literal.
	 * @param datatype
	 *        The datatype of the supplied label.
	 */
	public MemLiteral(Object creator, String label, URI datatype) {
		super(label, datatype);
		this.creator = creator;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Object getCreator() {
		return creator;
	}

	public MemStatementList getObjectStatementList() {
		if (objectStatements == null) {
			return EMPTY_LIST;
		}
		else {
			return objectStatements;
		}
	}

	public int getObjectStatementCount() {
		if (objectStatements == null) {
			return 0;
		}
		else {
			return objectStatements.size();
		}
	}

	public void addObjectStatement(MemStatement st) {
		if (objectStatements == null) {
			objectStatements = new MemStatementList(1);
		}

		objectStatements.add(st);
	}

	public void removeObjectStatement(MemStatement st) {
		objectStatements.remove(st);

		if (objectStatements.isEmpty()) {
			objectStatements = null;
		}
	}

	public void cleanSnapshotsFromObjectStatements(int currentSnapshot) {
		if (objectStatements != null) {
			objectStatements.cleanSnapshots(currentSnapshot);

			if (objectStatements.isEmpty()) {
				objectStatements = null;
			}
		}
	}
}
