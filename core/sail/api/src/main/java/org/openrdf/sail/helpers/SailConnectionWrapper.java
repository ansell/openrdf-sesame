/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.UnknownSailTransactionStateException;
import org.openrdf.sail.UpdateContext;

/**
 * An implementation of the SailConnection interface that wraps another
 * SailConnection object and forwards any method calls to the wrapped
 * connection.
 * 
 * @author Jeen Broekstra
 */
public class SailConnectionWrapper implements SailConnection {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The wrapped SailConnection.
	 */
	private SailConnection wrappedCon;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TransactionWrapper object that wraps the supplied
	 * connection.
	 */
	public SailConnectionWrapper(SailConnection wrappedCon) {
		this.wrappedCon = wrappedCon;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the connection that is wrapped by this object.
	 * 
	 * @return The SailConnection object that was supplied to the constructor of
	 *         this class.
	 */
	public SailConnection getWrappedConnection() {
		return wrappedCon;
	}

	public boolean isOpen()
		throws SailException
	{
		return wrappedCon.isOpen();
	}

	public void close()
		throws SailException
	{
		wrappedCon.close();
	}

	public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(TupleExpr tupleExpr,
			Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		return wrappedCon.evaluate(tupleExpr, dataset, bindings, includeInferred);
	}

	public CloseableIteration<? extends Resource, SailException> getContextIDs()
		throws SailException
	{
		return wrappedCon.getContextIDs();
	}

	public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, URI pred,
			Value obj, boolean includeInferred, Resource... contexts)
		throws SailException
	{
		return wrappedCon.getStatements(subj, pred, obj, includeInferred, contexts);
	}

	public long size(Resource... contexts)
		throws SailException
	{
		return wrappedCon.size(contexts);
	}

	public long size(Resource context)
		throws SailException
	{
		return wrappedCon.size(context);
	}

	public void commit()
		throws SailException
	{
		wrappedCon.commit();
	}

	public void rollback()
		throws SailException
	{
		wrappedCon.rollback();
	}

	public void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		wrappedCon.addStatement(subj, pred, obj, contexts);
	}

	public void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		wrappedCon.removeStatements(subj, pred, obj, contexts);
	}

	public void startUpdate(UpdateContext modify)
		throws SailException
	{
		wrappedCon.startUpdate(modify);
	}

	public void addStatement(UpdateContext modify, Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		wrappedCon.addStatement(modify, subj, pred, obj, contexts);
	}

	public void removeStatement(UpdateContext modify, Resource subj, URI pred, Value obj,
			Resource... contexts)
		throws SailException
	{
		wrappedCon.removeStatement(modify, subj, pred, obj, contexts);
	}

	public void endUpdate(UpdateContext modify)
		throws SailException
	{
		wrappedCon.endUpdate(modify);
	}

	public void clear(Resource... contexts)
		throws SailException
	{
		wrappedCon.clear(contexts);
	}

	public CloseableIteration<? extends Namespace, SailException> getNamespaces()
		throws SailException
	{
		return wrappedCon.getNamespaces();
	}

	public String getNamespace(String prefix)
		throws SailException
	{
		return wrappedCon.getNamespace(prefix);
	}

	public void setNamespace(String prefix, String name)
		throws SailException
	{
		wrappedCon.setNamespace(prefix, name);
	}

	public void removeNamespace(String prefix)
		throws SailException
	{
		wrappedCon.removeNamespace(prefix);
	}

	public void clearNamespaces()
		throws SailException
	{
		wrappedCon.clearNamespaces();
	}

	public void begin()
		throws SailException
	{
		wrappedCon.begin();
	}

	public void prepare()
		throws SailException
	{
		wrappedCon.prepare();
	}

	public boolean isActive()
		throws UnknownSailTransactionStateException
	{
		return wrappedCon.isActive();
	}
}
