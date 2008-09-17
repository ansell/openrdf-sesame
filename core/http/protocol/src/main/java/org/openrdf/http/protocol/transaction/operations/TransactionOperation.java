/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction.operations;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.StoreException;

/**
 * An update operation that is part of a transaction.
 * 
 * @author Arjohn Kampman
 * @author Leo Sauermann
 */
public interface TransactionOperation {

	/**
	 * Executes this operation on the supplied connection.
	 * 
	 * @param con
	 *        The connection the operation should be performed on.
	 * @throws StoreException
	 *         If such an exception is thrown by the connection while executing
	 *         the operation.
	 */
	public abstract void execute(RepositoryConnection con)
		throws StoreException;
}
