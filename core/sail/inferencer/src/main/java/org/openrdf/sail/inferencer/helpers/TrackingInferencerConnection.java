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
	protected InferencerConnection getDelegate() {
		return (InferencerConnection)super.getDelegate();
	}

	public boolean addInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		verifyIsOpen();
		modified();
		return getDelegate().addInferredStatement(subj, pred, obj, contexts);
	}

	public boolean removeInferredStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		verifyIsOpen();
		modified();
		return getDelegate().removeInferredStatements(subj, pred, obj, contexts);
	}

	public void flushUpdates()
		throws StoreException
	{
		verifyIsOpen();
		getDelegate().flushUpdates();
	}

	public void addConnectionListener(SailConnectionListener listener) {
		getDelegate().addConnectionListener(listener);
	}

	public void removeConnectionListener(SailConnectionListener listener) {
		getDelegate().removeConnectionListener(listener);
	}
}
