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
import org.openrdf.store.StoreException;

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
	public void begin()
		throws StoreException
	{
		super.begin();
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws StoreException
			{
				con.setAutoCommit(false);
			}
		});
	}

	@Override
	public void rollback()
		throws StoreException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws StoreException
			{
				con.rollback();
				con.setAutoCommit(true);
			}
		});
		super.rollback();
	}

	@Override
	public void commit()
		throws StoreException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws StoreException
			{
				con.commit();
				con.setAutoCommit(false);
			}
		});
		super.commit();
	}

	public void setNamespace(final String prefix, final String name)
		throws StoreException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws StoreException
			{
				if (!con.getRepository().getMetaData().isReadOnly()) {
					con.setNamespace(prefix, name);
				}
			}
		});
	}

	public void clearNamespaces()
		throws StoreException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws StoreException
			{
				if (!con.getRepository().getMetaData().isReadOnly()) {
					con.clearNamespaces();
				}
			}
		});
	}

	public void removeNamespace(final String prefix)
		throws StoreException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws StoreException
			{
				if (!con.getRepository().getMetaData().isReadOnly()) {
					con.removeNamespace(prefix);
				}
			}
		});
	}

	public void removeStatements(final Resource subj, final URI pred, final Value obj,
			final Resource... contexts)
		throws StoreException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws StoreException
			{
				if (!con.getRepository().getMetaData().isReadOnly()) {
					con.removeMatch(subj, pred, obj, contexts);
				}
			}
		});
	}
}
