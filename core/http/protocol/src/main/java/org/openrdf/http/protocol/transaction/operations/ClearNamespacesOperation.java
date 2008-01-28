/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction.operations;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Operation that removes all namespace declarations.
 * 
 * @author Arjohn Kampman
 */
public class ClearNamespacesOperation implements TransactionOperation {

	public ClearNamespacesOperation() {
	}

	public void execute(RepositoryConnection con)
		throws RepositoryException
	{
		con.clearNamespaces();
	}

	@Override
	public boolean equals(Object other)
	{
		return other instanceof ClearNamespacesOperation;
	}

	@Override
	public int hashCode()
	{
		return 101;
	}
}
