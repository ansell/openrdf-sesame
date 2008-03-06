/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
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
	private MemStatementList _objectStatements = null;

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
		_creator = creator;
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
		_creator = creator;
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
		_creator = creator;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Object getCreator() {
		return _creator;
	}

	public MemStatementList getObjectStatementList() {
		if (_objectStatements == null) {
			return EMPTY_LIST;
		}
		else {
			return _objectStatements;
		}
	}

	public int getObjectStatementCount() {
		if (_objectStatements == null) {
			return 0;
		}
		else {
			return _objectStatements.size();
		}
	}

	public void addObjectStatement(MemStatement st) {
		if (_objectStatements == null) {
			_objectStatements = new MemStatementList(1);
		}

		_objectStatements.add(st);
	}

	public void removeObjectStatement(MemStatement st) {
		_objectStatements.remove(st);

		if (_objectStatements.isEmpty()) {
			_objectStatements = null;
		}
	}
}
