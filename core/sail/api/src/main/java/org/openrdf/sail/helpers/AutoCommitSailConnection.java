/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailConnection;
import org.openrdf.store.StoreException;

/**
 * Adds auto-commit functionality to sail connections by wrapping updates with
 * calls to {@link #begin()} and {@link #commit()} when performed outside an
 * explicit transactions.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public class AutoCommitSailConnection extends SailConnectionWrapper {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public AutoCommitSailConnection(SailConnection con) {
		super(con);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public final void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		if (!isAutoCommit()) {
			super.addStatement(subj, pred, obj, contexts);
		}
		else {
			begin();
			try {
				super.addStatement(subj, pred, obj, contexts);
				commit();
			}
			finally {
				if (!isAutoCommit()) {
					// finish the transaction
					rollback();
				}
			}
		}
	}

	@Override
	public final void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		if (!isAutoCommit()) {
			super.removeStatements(subj, pred, obj, contexts);
		}
		else {
			begin();
			try {
				super.removeStatements(subj, pred, obj, contexts);
				commit();
			}
			finally {
				if (!isAutoCommit()) {
					// finish the transaction
					rollback();
				}
			}
		}
	}

	@Override
	public final void setNamespace(String prefix, String name)
		throws StoreException
	{
		if (!isAutoCommit()) {
			super.setNamespace(prefix, name);
		}
		else {
			begin();
			try {
				super.setNamespace(prefix, name);
				commit();
			}
			finally {
				if (!isAutoCommit()) {
					// finish the transaction
					rollback();
				}
			}
		}
	}

	@Override
	public final void removeNamespace(String prefix)
		throws StoreException
	{
		if (!isAutoCommit()) {
			super.removeNamespace(prefix);
		}
		else {
			begin();
			try {
				super.removeNamespace(prefix);
				commit();
			}
			finally {
				if (!isAutoCommit()) {
					// finish the transaction
					rollback();
				}
			}
		}
	}

	@Override
	public final void clearNamespaces()
		throws StoreException
	{
		if (!isAutoCommit()) {
			super.clearNamespaces();
		}
		else {
			begin();
			try {
				super.clearNamespaces();
				commit();
			}
			finally {
				if (!isAutoCommit()) {
					// finish the transaction
					rollback();
				}
			}
		}
	}

	@Override
	public void close()
		throws StoreException
	{
		if (isAutoCommit()) {
			super.close();
		}
		else {
			rollback();
			super.close();
		}
	}
}
