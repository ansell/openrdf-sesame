package org.openrdf.sail.federation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openrdf.model.LiteralFactory;
import org.openrdf.model.URIFactory;
import org.openrdf.model.impl.LiteralFactoryImpl;
import org.openrdf.model.impl.URIFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.helpers.PrefixHashSet;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailMetaData;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.store.StoreException;

/**
 * Union multiple (possibly remote) Repositories into a single RDF store.
 * 
 * @author James Leigh
 */
public class Federation extends SailBase {

	private URIFactory uf = new URIFactoryImpl();

	private LiteralFactory lf = new LiteralFactoryImpl();

	private List<Repository> members = new ArrayList<Repository>();

	private PrefixHashSet localPropertySpace;

	private boolean disjoint;

	private boolean writable = true;

	private FederatedMetaData metadata;

	public URIFactory getURIFactory() {
		return uf;
	}

	public LiteralFactory getLiteralFactory() {
		return lf;
	}

	public void addMember(Repository member) {
		members.add(member);
	}

	public PrefixHashSet getLocalPropertySpace() {
		return localPropertySpace;
	}

	public void setLocalPropertySpace(Collection<String> localPropertySpace) {
		this.localPropertySpace = new PrefixHashSet(localPropertySpace);
	}

	public boolean isDisjoint() {
		return disjoint;
	}

	public void setDisjoint(boolean disjoint) {
		this.disjoint = disjoint;
	}

	public void initialize()
		throws StoreException
	{
		for (Repository member : members) {
			member.initialize();
		}
	}

	@Override
	protected void shutDownInternal()
		throws StoreException
	{
		for (Repository member : members) {
			member.shutDown();
		}
	}

	public boolean isWritable()
		throws StoreException
	{
		return writable;
	}

	public void setWritable(boolean writable) {
		this.writable = writable;
	}

	@Override
	public FederatedMetaData getMetaData()
		throws StoreException
	{
		if (metadata != null)
			return metadata;
		return metadata = createMetaData();
	}

	@Override
	protected SailConnection getConnectionInternal()
		throws StoreException
	{
		List<RepositoryConnection> connections = new ArrayList<RepositoryConnection>(members.size());
		try {
			for (Repository member : members) {
				connections.add(member.getConnection());
			}
			if (writable) {
				return new RoundRobinConnection(this, connections);
			}
			else {
				return new ReadOnlyConnection(this, connections);
			}
		}
		catch (StoreException e) {
			closeAll(connections);
			throw e;
		}
		catch (RuntimeException e) {
			closeAll(connections);
			throw e;
		}
	}

	private FederatedMetaData createMetaData()
		throws StoreException
	{
		SailMetaData sailMetaData = super.getMetaData();
		FederatedMetaData metaData = new FederatedMetaData(sailMetaData, members);
		metaData.setReadOnly(!writable);
		return metaData;
	}

	private void closeAll(List<RepositoryConnection> connections) {
		for (RepositoryConnection con : connections) {
			try {
				con.close();
			}
			catch (StoreException e) {
				logger.error(e.toString(), e);
			}
		}
	}

}
