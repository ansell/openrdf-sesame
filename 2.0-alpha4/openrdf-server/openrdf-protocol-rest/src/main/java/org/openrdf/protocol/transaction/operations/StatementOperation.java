/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.protocol.transaction.operations;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

public abstract class StatementOperation extends TransactionOperation {

	protected Statement _statement;

	protected Resource _context;

	public StatementOperation(Statement stm, Resource context) {
		_statement = stm;
		_context = context;
	}

	/**
	 * both are null, you have to add context and statement by calling the
	 * methods. useful for parsing.
	 * 
	 */
	public StatementOperation() {

	}

	public StatementOperation(Statement stm) {
		this(stm, stm.getContext());
	}

	public Resource getContext() {
		return _context;
	}

	public Statement getStatement() {
		return _statement;
	}

	public void setContext(Resource context) {
		_context = context;
	}

	public void setStatement(Statement statement) {
		_statement = statement;
	}

	@Override
	public boolean equals(Object obj) {
		// they MUST be of the same class
		if (!this.getClass().equals(obj.getClass()))
			return false;

		StatementOperation o = ((StatementOperation) obj);
		if (!_statement.equals(o.getStatement()))
			return false;
		if (_context != null)
			return _context.equals(o.getContext());
		else
			return o.getContext() == null;
	}

}
