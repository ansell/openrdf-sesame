/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.protocol.transaction.operations;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * Remove a statement
 */
public class RemoveStatementOperation extends StatementOperation {

	public static final String XMLELEMENT = "remove";

	/**
	 * create the remove statement operation
	 * 
	 * @param stm
	 * @param context
	 */
	public RemoveStatementOperation(Statement stm, Resource context) {
		super(stm, context);
	}

	/**
	 * create the remove statement operation
	 */
	public RemoveStatementOperation(Statement stm) {
		super(stm);
	}

	/**
	 * create an empty operation. used as a placeholder in the parser
	 */
	public RemoveStatementOperation() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.sesame.sailimpl.http.operation.TransactionOperation#getOperationXMLElementName()
	 */
	@Override
	public String getOperationXMLElementName() {
		return XMLELEMENT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.sesame.sailimpl.http.operation.TransactionOperation#executeOperationOnTransaction(org.openrdf.sesame.sail.Transaction)
	 */
	@Override
	public void executeOperationOnTransaction(SailConnection transaction)
			throws SailException {
		transaction.removeStatement(_statement.getSubject(), _statement
				.getPredicate(), _statement.getObject(), _context);

	}

}
