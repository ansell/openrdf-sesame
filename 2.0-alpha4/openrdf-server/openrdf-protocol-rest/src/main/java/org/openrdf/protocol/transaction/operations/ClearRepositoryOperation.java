/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.protocol.transaction.operations;

import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;


/**
 * clear the whole repository.
 */
public class ClearRepositoryOperation extends TransactionOperation {

	public static final String XMLELEMENT = "clear";

	/**
	 * 
	 */
	public ClearRepositoryOperation() {
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
		transaction.clear();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof ClearRepositoryOperation);
	}

}
