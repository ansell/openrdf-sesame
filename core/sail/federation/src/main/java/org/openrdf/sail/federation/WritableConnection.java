/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.model.BNode;
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
class WritableConnection extends EchoWriteConnection {

	private int idx;

	Map<BNode, RepositoryConnection> owners = new ConcurrentHashMap<BNode, RepositoryConnection>();

	public WritableConnection(Federation federation, List<RepositoryConnection> members) {
		super(federation, members);
		int size = members.size();
		idx = (new Random().nextInt() % size + size) % size;
	}

	public void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		int i = findIndex(subj, pred, obj, contexts);
		try {
			add(members.get(i), subj, pred, obj, contexts);
		}
		catch (IllegalStatementException e) {
			int size = members.size();
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

	private int findIndex(Resource subj, URI pred, Value obj, Resource... contexts) {
		int size = members.size();
		if (isBNode(subj, obj, contexts)) {
			for (int i=0;i<size;i++) {
				// if one of the BNodes came from an existing member use that
				if (members.get(i).isSignedBNode(subj, pred, obj, contexts))
					return i;
			}
			// otherwise use a consistent member in case two BNodes need to be linked
			return 0;
		}
		// use round-robin for none-BNode statement to distribute the load
		int i = idx;
		idx = (i + 1) % size;
		return i;
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
