package org.openrdf.sail.federation;

import java.util.Collection;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.sail.SailReadOnlyException;
import org.openrdf.store.StoreException;

class ReadOnlyConnection extends FederationConnection {

	private boolean closed;

	public ReadOnlyConnection(Federation federation, Collection<RepositoryConnection> members) {
		super(federation, members);
	}

	public void begin()
		throws StoreException
	{
		// no-op
	}

	public boolean isOpen()
		throws StoreException
	{
		return !closed;
	}

	public void close()
		throws StoreException
	{
		super.close();
		closed = true;
	}

	public void rollback()
		throws StoreException
	{
		// no-op
	}

	public void commit()
		throws StoreException
	{
		// no-op
	}

	public void setNamespace(String prefix, String name)
		throws StoreException
	{
		throw new SailReadOnlyException();
	}

	public void clearNamespaces()
		throws StoreException
	{
		throw new SailReadOnlyException();
	}

	public void removeNamespace(String prefix)
		throws StoreException
	{
		throw new SailReadOnlyException();
	}

	public void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		throw new SailReadOnlyException();
	}

	public void removeStatements(Resource subj, URI pred, Value obj, Resource... context)
		throws StoreException
	{
		throw new SailReadOnlyException();
	}

}
