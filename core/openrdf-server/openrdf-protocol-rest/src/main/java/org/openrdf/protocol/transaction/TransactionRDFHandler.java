/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.protocol.transaction;

import java.util.logging.Logger;

import org.openrdf.model.Statement;
import org.openrdf.protocol.transaction.operations.OperationList;
import org.openrdf.protocol.transaction.operations.StatementOperation;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * handles statements parsed by the RDF parser. Statements are either added or
 * removed, so this creates add or remove operations.
 * 
 */
public class TransactionRDFHandler implements RDFHandler {

	public static final Logger log = Logger
			.getLogger(TransactionRDFHandler.class.getName());

	/**
	 * add the parsed statements as operations here
	 */
	private OperationList addhere;

	private Class<? extends StatementOperation> opclass = null;

	public TransactionRDFHandler(OperationList addhere,
			Class<? extends StatementOperation> opclass) {
		super();
		this.addhere = addhere;
		this.opclass = opclass;
	}

	public void endRDF() throws RDFHandlerException {
	}

	public void handleNamespace(String prefix, String uri)
			throws RDFHandlerException {
	}

	public void handleStatement(Statement st) throws RDFHandlerException {
		try {
			/**
			 * create the new needed operation and add it to the list
			 */
			StatementOperation op = opclass.newInstance();
			op.setStatement(st);
			op.setContext(st.getContext());
			addhere.add(op);
		} catch (InstantiationException e) {
			log.warning("programming error: " + e);
		} catch (IllegalAccessException e) {
			log.warning("programming error: " + e);
		}
	}

	public void startRDF() throws RDFHandlerException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.rio.RDFHandler#handleComment(java.lang.String)
	 */
	public void handleComment(String arg0) throws RDFHandlerException {
		// TODO Auto-generated method stub

	}

}
