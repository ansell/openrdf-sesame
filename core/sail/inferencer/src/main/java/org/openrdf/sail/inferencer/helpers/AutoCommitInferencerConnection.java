/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.inferencer.helpers;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.helpers.AutoCommitNotifyingConnection;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.store.StoreException;

/**
 * Adds auto-commit functionality to sail connections by wrapping updates with
 * calls to {@link #begin()} and {@link #commit()} when performed outside an
 * explicit transactions.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public class AutoCommitInferencerConnection extends AutoCommitNotifyingConnection implements
		InferencerConnection
{

	/*--------------*
	 * Constructors *
	 *--------------*/

	public AutoCommitInferencerConnection(InferencerConnection con) {
		super(con);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected InferencerConnection getDelegate() {
		return (InferencerConnection)super.getDelegate();
	}

	public boolean addInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		if (!isAutoCommit()) {
			return getDelegate().addInferredStatement(subj, pred, obj, contexts);
		}
		else {
			begin();
			try {
				boolean result = getDelegate().addInferredStatement(subj, pred, obj, contexts);
				commit();
				return result;
			}
			finally {
				if (!isAutoCommit()) {
					// finish the transaction
					rollback();
				}
			}
		}
	}

	public boolean removeInferredStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		if (!isAutoCommit()) {
			return getDelegate().removeInferredStatements(subj, pred, obj, contexts);
		}
		else {
			begin();
			try {
				boolean result = getDelegate().removeInferredStatements(subj, pred, obj, contexts);
				commit();
				return result;
			}
			finally {
				if (!isAutoCommit()) {
					// finish the transaction
					rollback();
				}
			}
		}
	}

	public void flushUpdates()
		throws StoreException
	{
		getDelegate().flushUpdates();
	}
}
