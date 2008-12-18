package org.openrdf.sail.federation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.exceptions.IllegalStatementException;
import org.openrdf.sail.federation.members.MemberConnection;
import org.openrdf.store.StoreException;

/**
 * Statements are only written to a single member. Statements that have a
 * {@link IllegalStatementException} throw when added to a member are tried
 * against all other members until it is accepted. If no members accept a
 * statement the original exception is re-thrown.
 * 
 * @author James Leigh
 */
class WritableConnection extends EchoConnection {

	private static List<RepositoryConnection> wrap(List<RepositoryConnection> members, BNodeFactoryImpl bf) {
		List<RepositoryConnection> result = new ArrayList<RepositoryConnection>(members.size());
		for (RepositoryConnection member : members) {
			result.add(new MemberConnection(member, bf));
		}
		return result;
	}

	private int idx;

	Map<BNode, RepositoryConnection> owners = new ConcurrentHashMap<BNode, RepositoryConnection>();

	public WritableConnection(Federation federation, List<RepositoryConnection> members) {
		this(federation, members, new BNodeFactoryImpl());
	}

	private WritableConnection(Federation federation, List<RepositoryConnection> members, BNodeFactoryImpl bf)
	{
		super(federation, wrap(members, bf), bf);
		int size = members.size();
		idx = (new Random().nextInt() % size + size) % size;
	}

	public void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		int size = members.size();
		int i = 1;
		// only use round-robin for none-BNode statement
		if (!isBNode(subj, obj, contexts)) {
			i = idx;
			idx = (i + 1) % size;
		}
		try {
			add(members.get(i), subj, pred, obj, contexts);
		}
		catch (IllegalStatementException e) {
			for (int j = i + 1; j < i + size; j++) {
				try {
					add(members.get(j % size), subj, pred, obj, contexts);
					return;
				}
				catch (IllegalStatementException e2) {
					continue;
				}
			}
			throw e;
		}
	}

	private boolean isBNode(Resource subj, Value obj, Resource... contexts) {
		if (subj instanceof BNode)
			return true;
		if (obj instanceof BNode)
			return true;
		if (contexts != null) {
			for (Resource ctx : contexts) {
				if (ctx instanceof BNode)
					return true;
			}
		}
		return false;
	}

	private void add(RepositoryConnection member, Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		checkOwnership(member, subj, obj, contexts);
		member.add(subj, pred, obj, contexts);
		recordOwnership(member, subj, obj, contexts);
	}

	private void checkOwnership(RepositoryConnection member, Resource subj, Value obj, Resource... contexts)
		throws IllegalStatementException
	{
		if (!owners.isEmpty()) {
			if (notEqual(member, owners.get(subj)))
				throw illegal(subj, obj, contexts);
			if (notEqual(member, owners.get(obj)))
				throw illegal(subj, obj, contexts);
			if (contexts != null) {
				for (Resource ctx : contexts) {
					if (ctx != null && notEqual(member, owners.get(ctx)))
						throw illegal(subj, obj, contexts);
				}
			}
		}
	}

	private IllegalStatementException illegal(Resource subj, Value obj, Resource... contexts) {
		return new IllegalStatementException("Cannot combine " + subj + " and " + obj + " in context "
				+ Arrays.toString(contexts));
	}

	private void recordOwnership(RepositoryConnection member, Resource subj, Value obj, Resource... contexts) {
		if (subj instanceof BNode) {
			owners.put((BNode)subj, member);
		}
		if (obj instanceof BNode) {
			owners.put((BNode)obj, member);
		}
		if (contexts != null) {
			for (Resource ctx : contexts) {
				if (ctx instanceof BNode) {
					owners.put((BNode)ctx, member);
				}
			}
		}
	}

	private boolean notEqual(RepositoryConnection o1, RepositoryConnection o2) {
		return o1 != null && o2 != null && !o1.equals(o2);
	}

}
