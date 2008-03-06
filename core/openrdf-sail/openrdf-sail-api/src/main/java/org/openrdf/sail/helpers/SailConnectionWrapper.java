/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
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
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;

/**
 * An implementation of the Transaction interface that wraps another Transaction
 * object and forwards any method calls to the wrapped transaction.
 * 
 * @author jeen
 */
public class SailConnectionWrapper implements SailConnection {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The wrapped SailConnection.
	 */
	private SailConnection _wrappedCon;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TransactionWrapper object that wraps the supplied
	 * connection.
	 */
	public SailConnectionWrapper(SailConnection wrappedCon) {
		_wrappedCon = wrappedCon;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the Connection that is wrapped by this object.
	 * 
	 * @return The SailConnection object that was supplied to the constructor of
	 *         this class.
	 */
	protected SailConnection getWrappedConnection() {
		return _wrappedCon;
	}

	public boolean isOpen()
		throws SailException
	{
		return _wrappedCon.isOpen();
	}

	public void close()
		throws SailException
	{
		_wrappedCon.close();
	}

	public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(TupleExpr tupleExpr, BindingSet bindings,
			boolean includeInferred)
		throws SailException
	{
		return _wrappedCon.evaluate(tupleExpr, bindings, includeInferred);
	}

	public CloseableIteration<? extends Resource, SailException> getContextIDs()
		throws SailException
	{
		return _wrappedCon.getContextIDs();
	}

	public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, URI pred, Value obj,
			boolean useInference, Resource... contexts)
		throws SailException
	{
		return _wrappedCon.getStatements(subj, pred, obj, useInference, contexts);
	}

	public long size(Resource... contexts)
		throws SailException
	{
		return _wrappedCon.size(contexts);
	}

	public long size(Resource context)
		throws SailException
	{
		return _wrappedCon.size(context);
	}

	public void commit()
		throws SailException
	{
		_wrappedCon.commit();
	}

	public void rollback()
		throws SailException
	{
		_wrappedCon.rollback();
	}

	public void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		_wrappedCon.addStatement(subj, pred, obj, contexts);
	}

	public void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		_wrappedCon.removeStatements(subj, pred, obj, contexts);
	}

	public void clear(Resource... contexts)
		throws SailException
	{
		_wrappedCon.clear(contexts);
	}

	public CloseableIteration<? extends Namespace, SailException> getNamespaces()
		throws SailException
	{
		return _wrappedCon.getNamespaces();
	}

	public String getNamespace(String prefix)
		throws SailException
	{
		return _wrappedCon.getNamespace(prefix);
	}

	public void setNamespace(String prefix, String name)
		throws SailException
	{
		_wrappedCon.setNamespace(prefix, name);
	}

	public void removeNamespace(String prefix)
		throws SailException
	{
		_wrappedCon.removeNamespace(prefix);
	}

	public void addConnectionListener(SailConnectionListener listener) {
		_wrappedCon.addConnectionListener(listener);
	}

	public void removeConnectionListener(SailConnectionListener listener) {
		_wrappedCon.addConnectionListener(listener);
	}
}
