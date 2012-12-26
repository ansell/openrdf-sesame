/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation;

import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailException;

/**
 * Echos all write operations to all members.
 * 
 * @author James Leigh
 */
abstract class EchoWriteConnection extends FederationConnection {

	public EchoWriteConnection(Federation federation, List<RepositoryConnection> members) {
		super(federation, members);
	}

	@Override
	public void startTransactionInternal() throws SailException {
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws RepositoryException
			{
				con.setAutoCommit(false);
			}
		});
	}

	@Override
	public void rollbackInternal()
		throws SailException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws RepositoryException
			{
				con.rollback();
				con.setAutoCommit(true);
			}
		});
	}

	@Override
	public void commitInternal()
		throws SailException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws RepositoryException
			{
				con.setAutoCommit(true);
			}
		});
	}

	public void setNamespaceInternal(final String prefix, final String name)
		throws SailException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws RepositoryException
			{
				con.setNamespace(prefix, name);
			}
		});
	}

	@Override
	public void clearNamespacesInternal()
		throws SailException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws RepositoryException
			{
				con.clearNamespaces();
			}
		});
	}

	@Override
	public void removeNamespaceInternal(final String prefix)
		throws SailException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws RepositoryException
			{
				con.removeNamespace(prefix);
			}
		});
	}

	@Override
	public void removeStatementsInternal(final Resource subj, final URI pred, final Value obj,
			final Resource... contexts)
		throws SailException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws RepositoryException
			{
				con.remove(subj, pred, obj, contexts);
			}
		});
	}
}
