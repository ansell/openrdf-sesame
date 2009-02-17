/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.inferencer.helpers;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.helpers.PreconditionSailConnection;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.store.StoreException;

/**
 * Extension of {@link PreconditionSailConnection} that adds inferencer supoprt.
 * 
 * @author Arjohn Kampman
 */
public class PreconditionInferencerConnection extends PreconditionSailConnection implements
		InferencerConnection
{

	/*--------------*
	 * Constructors *
	 *--------------*/

	public PreconditionInferencerConnection(InferencerConnection con) {
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
		verifyIsOpen();
		verifyNotReadOnly();
		return getDelegate().addInferredStatement(subj, pred, obj, contexts);
	}

	public boolean removeInferredStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		verifyIsOpen();
		verifyNotReadOnly();
		return getDelegate().removeInferredStatements(subj, pred, obj, contexts);
	}

	public void flushUpdates()
		throws StoreException
	{
		verifyIsOpen();
		// verifyActive();
		getDelegate().flushUpdates();
	}

	public void addConnectionListener(SailConnectionListener listener) {
		getDelegate().addConnectionListener(listener);
	}

	public void removeConnectionListener(SailConnectionListener listener) {
		getDelegate().removeConnectionListener(listener);
	}
}
