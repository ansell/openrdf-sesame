/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation;

import java.util.List;
import java.util.Random;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailException;

/**
 * Statements are only written to a single member. Statements that have a
 * {@link IllegalStatementException} throw when added to a member are tried
 * against all other members until it is accepted. If no members accept a
 * statement the original exception is re-thrown.
 * 
 * @author James Leigh
 */
class WritableConnection extends AbstractEchoWriteConnection {
	private int addIndex;

	public WritableConnection(Federation federation,
			List<RepositoryConnection> members) throws SailException {
		super(federation, members);
		int size = members.size();
		int rnd = (new Random().nextInt() % size + size) % size;
		// use round-robin to distribute the load
		for (int i = rnd, n = rnd + size; i < n; i++) {
			try {
				if (members.get(i % size).getRepository().isWritable()) {
					addIndex = i % size;
				}
			} catch (RepositoryException e) {
				throw new SailException(e);
			}
		}
	}

	@Override
	public void addStatementInternal(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		add(members.get(addIndex), subj, pred, obj, contexts);
	}

	private void add(RepositoryConnection member, Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		try {
			member.add(subj, pred, obj, contexts);
		} catch (RepositoryException e) {
			throw new SailException(e);
		}
	}

	@Override
	protected void clearInternal(Resource... contexts) throws SailException {
		removeStatementsInternal(null, null, null, contexts);
	}

}
