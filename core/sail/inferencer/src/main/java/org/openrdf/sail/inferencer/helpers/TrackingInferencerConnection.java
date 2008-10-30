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
import org.openrdf.sail.helpers.SailConnectionTracker;
import org.openrdf.sail.helpers.TrackingSailConnection;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.store.StoreException;

/**
 * Tracks SailConnection iterations and verifies that the connection is open.
 * 
 * @author Arjohn Kampman
 * @author jeen
 * @author James Leigh
 */
public class TrackingInferencerConnection extends TrackingSailConnection implements InferencerConnection {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public TrackingInferencerConnection(InferencerConnection con, SailConnectionTracker tracker) {
		super(con, tracker);
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
		verifyIsOpen();
		autoStartTransaction();
		return getWrappedConnection().addInferredStatement(subj, pred, obj, contexts);
	}

	public boolean removeInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		verifyIsOpen();
		autoStartTransaction();
		return getWrappedConnection().removeInferredStatement(subj, pred, obj, contexts);
	}

	public void flushUpdates()
		throws StoreException
	{
		verifyIsOpen();
		getWrappedConnection().flushUpdates();
	}

	public void addConnectionListener(SailConnectionListener listener) {
		getWrappedConnection().addConnectionListener(listener);
	}

	public void removeConnectionListener(SailConnectionListener listener) {
		getWrappedConnection().removeConnectionListener(listener);
	}
}
