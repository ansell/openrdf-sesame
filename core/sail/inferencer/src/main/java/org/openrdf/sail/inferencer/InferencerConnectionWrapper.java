/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.inferencer;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.StoreException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.helpers.NotifyingSailConnectionWrapper;

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
		throws StoreException
	{
		return getWrappedConnection().addInferredStatement(subj, pred, obj, contexts);
	}

	public boolean removeInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		return getWrappedConnection().removeInferredStatement(subj, pred, obj, contexts);
	}

	public void clearInferred(Resource... contexts)
		throws StoreException
	{
		getWrappedConnection().clearInferred(contexts);
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
	public CloseableIteration<? extends BindingSet, StoreException> evaluate(TupleExpr tupleExpr,
			Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws StoreException
	{
		flushUpdates();
		return super.evaluate(tupleExpr, dataset, bindings, includeInferred);
	}

	/**
	 * Calls {@link #flushUpdates()} before forwarding the call to the wrapped
	 * connection.
	 */
	@Override
	public CloseableIteration<? extends Resource, StoreException> getContextIDs()
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
	public CloseableIteration<? extends Statement, StoreException> getStatements(Resource subj, URI pred,
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
	public long size(Resource... contexts)
		throws StoreException
	{
		flushUpdates();
		return super.size(contexts);
	}
}
