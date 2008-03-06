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
 * Add a statement
 */
public class AddStatementOperation extends StatementOperation {

	public static final String XMLELEMENT = "add";

	/**
	 * create the add statement operation
	 * 
	 * @param stm
	 * @param context
	 */
	public AddStatementOperation(Statement stm, Resource context) {
		super(stm, context);
	}

	/**
	 * create the add statement operation
	 * 
	 * @param stm
	 * @param context
	 */
	public AddStatementOperation(Statement stm) {
		super(stm, stm.getContext());
	}

	/**
	 * create an empty add statement operation. used in the parser as
	 * placeholder.
	 */
	public AddStatementOperation() {
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
		transaction.addStatement(_statement.getSubject(), _statement
				.getPredicate(), _statement.getObject(), _context);

	}

}
