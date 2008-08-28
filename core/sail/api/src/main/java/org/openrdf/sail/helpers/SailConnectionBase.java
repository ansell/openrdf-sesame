/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.StoreException;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.inferencer.InferencerConnection;

/**
 * Abstract Class offering base functionality for SailConnection
 * implementations.
 * 
 * @author Arjohn Kampman
 * @author jeen
 * @author James Leigh
 */
public abstract class SailConnectionBase implements SailConnection {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean txnActive;

	/*---------*
	 * Methods *
	 *---------*/

	public boolean isInferencingSupported() {
		return this instanceof InferencerConnection;
	}

	public boolean isNotifyingSupported() {
		return this instanceof NotifyingSailConnection;
	}

	public boolean isOpen()
		throws StoreException
	{
		return true;
	}

	public final void close()
		throws StoreException
	{
		closeInternal();
	}

	public final CloseableIteration<? extends BindingSet, StoreException> evaluate(
			TupleExpr tupleExpr, Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws StoreException
	{
		return evaluateInternal(tupleExpr, dataset, bindings, includeInferred);
	}

	public final CloseableIteration<? extends Resource, StoreException> getContextIDs()
		throws StoreException
	{
		return getContextIDsInternal();
	}

	public final CloseableIteration<? extends Statement, StoreException> getStatements(Resource subj, URI pred,
			Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		return getStatementsInternal(subj, pred, obj, includeInferred, contexts);
	}

	public final long size(Resource... contexts)
		throws StoreException
	{
		return sizeInternal(contexts);
	}

	protected boolean transactionActive() {
		return txnActive;
	}

	public void begin()
		throws StoreException
	{
		txnActive = true;
	}

	public final void commit()
		throws StoreException
	{
		commitInternal();
		txnActive = false;
	}

	public final void rollback()
		throws StoreException
	{
		rollbackInternal();
		txnActive = false;
	}

	public final void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		addStatementInternal(subj, pred, obj, contexts);
	}

	public final void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		removeStatementsInternal(subj, pred, obj, contexts);
	}

	public final void clear(Resource... contexts)
		throws StoreException
	{
		clearInternal(contexts);
	}

	public final CloseableIteration<? extends Namespace, StoreException> getNamespaces()
		throws StoreException
	{
		return getNamespacesInternal();
	}

	public final String getNamespace(String prefix)
		throws StoreException
	{
		return getNamespaceInternal(prefix);
	}

	public final void setNamespace(String prefix, String name)
		throws StoreException
	{
		setNamespaceInternal(prefix, name);
	}

	public final void removeNamespace(String prefix)
		throws StoreException
	{
		removeNamespaceInternal(prefix);
	}

	public final void clearNamespaces()
		throws StoreException
	{
		clearNamespacesInternal();
	}

	protected abstract void closeInternal()
		throws StoreException;

	protected abstract CloseableIteration<? extends BindingSet, StoreException> evaluateInternal(
			TupleExpr tupleExpr, Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws StoreException;

	protected abstract CloseableIteration<? extends Resource, StoreException> getContextIDsInternal()
		throws StoreException;

	protected abstract CloseableIteration<? extends Statement, StoreException> getStatementsInternal(
			Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException;

	protected abstract long sizeInternal(Resource... contexts)
		throws StoreException;

	protected abstract void commitInternal()
		throws StoreException;

	protected abstract void rollbackInternal()
		throws StoreException;

	protected abstract void addStatementInternal(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException;

	protected abstract void removeStatementsInternal(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException;

	protected abstract void clearInternal(Resource... contexts)
		throws StoreException;

	protected abstract CloseableIteration<? extends Namespace, StoreException> getNamespacesInternal()
		throws StoreException;

	protected abstract String getNamespaceInternal(String prefix)
		throws StoreException;

	protected abstract void setNamespaceInternal(String prefix, String name)
		throws StoreException;

	protected abstract void removeNamespaceInternal(String prefix)
		throws StoreException;

	protected abstract void clearNamespacesInternal()
		throws StoreException;
}
