/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.inferencer.helpers;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Cursor;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.helpers.NotifyingSailConnectionWrapper;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.store.StoreException;

/**
 * An extension of ConnectionWrapper that implements the
 * {@link InferencerConnection} interface.
 * 
 * @author Arjohn Kampman
 */
public class InferencerConnectionWrapper extends NotifyingSailConnectionWrapper implements
		InferencerConnection
{

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new wrapper object that wraps the supplied connection.
	 */
	public InferencerConnectionWrapper(SailConnection con) {
		super(con);
	}

	/**
	 * Creates a new InferencerConnectionWrapper object that wraps the supplied
	 * connection and ensures the connection supports inferencinc.
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
		throws StoreException
	{
		return getWrappedConnection().addInferredStatement(subj, pred, obj, contexts);
	}

	public boolean removeInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		return getWrappedConnection().removeInferredStatement(subj, pred, obj, contexts);
	}

	public void flushUpdates()
		throws StoreException
	{
		getWrappedConnection().flushUpdates();
	}

	/**
	 * Calls {@link #flushUpdates()} before forwarding the call to the wrapped
	 * connection.
	 */
	@Override
	public void commit()
		throws StoreException
	{
		flushUpdates();
		super.commit();
	}

	/**
	 * Calls {@link #flushUpdates()} before forwarding the call to the wrapped
	 * connection.
	 */
	@Override
	public Cursor<? extends BindingSet> evaluate(QueryModel query, BindingSet bindings, boolean includeInferred)
		throws StoreException
	{
		flushUpdates();
		return super.evaluate(query, bindings, includeInferred);
	}

	/**
	 * Calls {@link #flushUpdates()} before forwarding the call to the wrapped
	 * connection.
	 */
	@Override
	public Cursor<? extends Resource> getContextIDs()
		throws StoreException
	{
		flushUpdates();
		return super.getContextIDs();
	}

	/**
	 * Calls {@link #flushUpdates()} before forwarding the call to the wrapped
	 * connection.
	 */
	@Override
	public Cursor<? extends Statement> getStatements(Resource subj, URI pred,
			Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		flushUpdates();
		return super.getStatements(subj, pred, obj, includeInferred, contexts);
	}

	/**
	 * Calls {@link #flushUpdates()} before forwarding the call to the wrapped
	 * connection.
	 */
	@Override
	public long size(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		flushUpdates();
		return super.size(subj, pred, obj, includeInferred, contexts);
	}
}
