package org.openrdf.sail.federation;

import java.util.List;
import java.util.Random;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.store.StoreException;

class RoundRobinConnection extends WriteToAllConnection {

	private List<RepositoryConnection> members;

	private int idx;

	public RoundRobinConnection(Federation federation, List<RepositoryConnection> members) {
		super(federation, members);
		this.members = members;
		int size = members.size();
		idx = (new Random().nextInt() % size + size) % size;
	}

	public void addStatement(final Resource subj, final URI pred, final Value obj, final Resource... contexts)
		throws StoreException
	{
		roundRobin(new Procedure() {

			public void run(RepositoryConnection member)
				throws StoreException
			{
				member.add(subj, pred, obj, contexts);
			}
		});
	}

	private void roundRobin(Procedure operation)
		throws StoreException
	{
		int i = idx;
		RepositoryConnection member = members.get(i);
		idx = (i + 1) % members.size();
		operation.run(member);
	}

}
