/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.inferencer;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionWrapper;

/**
 * An extension of ConnectionWrapper that implements the
 * {@link InferencerConnection} interface.
 */
public class InferencerConnectionWrapper extends SailConnectionWrapper implements InferencerConnection {

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new InferencerConnectionWrapper object that wraps the supplied
	 * transaction.
	 */
	public InferencerConnectionWrapper(InferencerConnection con) {
		super(con);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the connection that is wrapped by this object.
	 * 
	 * @return The connection that was supplied to the constructor of this class.
	 */
	@Override
	protected InferencerConnection getWrappedConnection() {
		return (InferencerConnection)super.getWrappedConnection();
	}

	public boolean addInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		return getWrappedConnection().addInferredStatement(subj, pred, obj, contexts);
	}

	public boolean removeInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		return getWrappedConnection().removeInferredStatement(subj, pred, obj, contexts);
	}

	public void clearInferred(Resource... contexts)
		throws SailException
	{
		getWrappedConnection().clearInferred(contexts);
	}

	public void flushUpdates()
		throws SailException
	{
		getWrappedConnection().flushUpdates();
	}

	/**
	 * Calls {@link InferencerConnection#flushUpdates()} before forwarding the
	 * commit to the wrapped connection.
	 */
	@Override
	public void commit()
		throws SailException
	{
		flushUpdates();
		super.commit();
	}
}
