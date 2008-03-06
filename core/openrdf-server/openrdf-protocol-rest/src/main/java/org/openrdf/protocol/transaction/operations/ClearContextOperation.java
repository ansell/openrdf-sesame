/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.protocol.transaction.operations;

import org.openrdf.model.Resource;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * clear a context
 */
public class ClearContextOperation extends TransactionOperation {

	public static final String XMLELEMENT = "clearcontext";

	protected Resource _context;

	/**
	 * 
	 */
	public ClearContextOperation(Resource context) {
		super();
		_context = context;
	}

	/**
	 * emtpy constructor, for parsing. Set the context!
	 * 
	 */
	public ClearContextOperation() {

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
		transaction.clearContext(_context);

	}

	public Resource getContext() {
		return _context;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ClearContextOperation))
			return false;
		return getContext().equals(((ClearContextOperation) obj).getContext());
	}

	public void setContext(Resource context) {
		_context = context;
	}

}
