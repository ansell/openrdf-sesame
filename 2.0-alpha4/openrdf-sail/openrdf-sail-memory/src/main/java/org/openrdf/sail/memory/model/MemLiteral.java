/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;

/**
 * A MemoryStore-specific extension of Literal giving it node properties.
 */
public class MemLiteral extends LiteralImpl implements MemValue {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The object that created this MemLiteral.
	 */
	private Object _creator;

	/**
	 * The list of statements for which this MemLiteral is the object.
	 */
	private MemStatementList _objectStatements = new MemStatementList(1);

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new Literal which will get the supplied label.
	 *
	 * @param creator The object that is creating this MemLiteral.
	 * @param label The label for this literal.
	 */
	public MemLiteral(Object creator, String label) {
		super(label);
		_creator = creator;
	}

	/**
	 * Creates a new Literal which will get the supplied label and language
	 * code.
	 *
	 * @param creator The object that is creating this MemLiteral.
	 * @param label The label for this literal.
	 * @param lang The language code of the supplied label.
	 */
	public MemLiteral(Object creator, String label, String lang) {
		super(label, lang);
		_creator = creator;
	}

	/**
	 * Creates a new Literal which will get the supplied label and datatype.
	 *
	 * @param creator The object that is creating this MemLiteral.
	 * @param label The label for this literal.
	 * @param datatype The datatype of the supplied label.
	 */
	public MemLiteral(Object creator, String label, URI datatype) {
		super(label, datatype);
		_creator = creator;
	}

	/*---------*
	 * Methods *
	 *---------*/

	// Implements MemValue.getCreator()
    public Object getCreator() {
        return _creator;
    }

	// Implements MemValue.getObjectStatementList()
	public MemStatementList getObjectStatementList() {
		return _objectStatements;
	}

	// Implements MemValue.getObjectStatementCount()
	public int getObjectStatementCount() {
		return _objectStatements.size();
	}

	// Implements MemValue.addObjectStatement(MemStatement)
	public void addObjectStatement(MemStatement st) {
		_objectStatements.add(st);
	}

	// Implements MemValue.removeObjectStatement(MemStatement)
	public void removeObjectStatement(MemStatement st) {
		_objectStatements.remove(st);
	}
}
