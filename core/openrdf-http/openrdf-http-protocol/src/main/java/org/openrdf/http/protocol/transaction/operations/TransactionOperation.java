/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction.operations;

import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * An update operation that is part of a transaction.
 * 
 * @author Arjohn Kampman
 */
public interface TransactionOperation {

	/**
	 * Executes this operation on the supplied connection.
	 * 
	 * @param con
	 *        The connection the operation should be performed on.
	 * @throws SailException
	 *         If such an exception is thrown by the connection while executing
	 *         the operation.
	 */
	public abstract void execute(SailConnection con)
		throws SailException;
}
