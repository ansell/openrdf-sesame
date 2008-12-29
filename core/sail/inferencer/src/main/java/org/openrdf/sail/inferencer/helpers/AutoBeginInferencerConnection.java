/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.inferencer.helpers;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.helpers.AutoBeginSailConnection;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.store.StoreException;

/**
 * Auto begins and commits transactions.
 * 
 * @author James Leigh
 */
public class AutoBeginInferencerConnection extends AutoBeginSailConnection implements InferencerConnection {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public AutoBeginInferencerConnection(InferencerConnection con) {
		super(con);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected InferencerConnection getWrappedConnection() {
		return (InferencerConnection)super.getWrappedConnection();
	}

	public boolean addInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		InferencerConnection con = getWrappedConnection();
		if (isActive()) {
			return con.addInferredStatement(subj, pred, obj, contexts);
		}
		else {
			try {
				begin();
				boolean result = con.addInferredStatement(subj, pred, obj, contexts);
				commit();
				return result;
			}
			catch (RuntimeException e) {
				rollback();
				throw e;
			}
			catch (StoreException e) {
				rollback();
				throw e;
			}
		}
	}

	public boolean removeInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		InferencerConnection con = getWrappedConnection();
		if (isActive()) {
			return con.removeInferredStatement(subj, pred, obj, contexts);
		}
		else {
			try {
				begin();
				boolean result = con.removeInferredStatement(subj, pred, obj, contexts);
				commit();
				return result;
			}
			catch (RuntimeException e) {
				rollback();
				throw e;
			}
			catch (StoreException e) {
				rollback();
				throw e;
			}
		}
	}

	public void flushUpdates()
		throws StoreException
	{
		getWrappedConnection().flushUpdates();
	}

	public void addConnectionListener(SailConnectionListener listener) {
		getWrappedConnection().addConnectionListener(listener);
	}

	public void removeConnectionListener(SailConnectionListener listener) {
		getWrappedConnection().removeConnectionListener(listener);
	}
}
