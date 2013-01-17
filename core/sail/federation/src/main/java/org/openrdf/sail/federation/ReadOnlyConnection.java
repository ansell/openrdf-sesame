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
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailReadOnlyException;

/**
 * Finishes the {@link AbstractFederationConnection} by throwing
 * {@link SailReadOnlyException}s for all write operations.
 * 
 * @author James Leigh
 */
class ReadOnlyConnection extends AbstractFederationConnection {

	public ReadOnlyConnection(Federation federation, List<RepositoryConnection> members) {
		super(federation, members);
	}

	@Override
	public void setNamespaceInternal(String prefix, String name)
		throws SailException
	{
		throw new SailReadOnlyException("");
	}

	@Override
	public void clearNamespacesInternal()
		throws SailException
	{
		throw new SailReadOnlyException("");
	}

	@Override
	public void removeNamespaceInternal(String prefix)
		throws SailException
	{
		throw new SailReadOnlyException("");
	}

	@Override
	public void addStatementInternal(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		throw new SailReadOnlyException("");
	}

	@Override
	public void removeStatementsInternal(Resource subj, URI pred, Value obj, Resource... context)
		throws SailException
	{
		throw new SailReadOnlyException("");
	}

	@Override
	protected void clearInternal(Resource... contexts) throws SailException {
		throw new SailReadOnlyException("");
	}

	@Override
	protected void commitInternal() throws SailException {
		// no-op
	}

	@Override
	protected void rollbackInternal() throws SailException {
		// no-op
	}

	@Override
	protected void startTransactionInternal() throws SailException {
		// no-op
	}
}
