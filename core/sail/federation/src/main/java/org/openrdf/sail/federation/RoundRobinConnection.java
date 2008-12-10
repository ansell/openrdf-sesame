package org.openrdf.sail.federation;

import java.util.List;
import java.util.Random;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.exceptions.IllegalStatementException;
import org.openrdf.store.StoreException;

/**
 * Statements are only written to a single member. Statements that have a
 * {@link IllegalStatementException} throw when added to a member are tried
 * against all other members until it is accepted. If no members accept a
 * statement the original exception is re-thrown.
 * 
 * @author James Leigh
 */
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
		int size = members.size();
		int i = idx;
		idx = (i + 1) % size;
		try {
			operation.run(members.get(i));
		}
		catch (IllegalStatementException e) {
			for (int j = i + 1; j < i + size; j++) {
				try {
					operation.run(members.get(j % size));
					return;
				}
				catch (IllegalStatementException e2) {
					continue;
				}
			}
			throw e;
		}
	}

}
