/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import org.openrdf.cursor.Cursor;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.sail.SailConnection;
import org.openrdf.store.Isolation;
import org.openrdf.store.StoreException;

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
	private final SailConnection delegate;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TransactionWrapper object that wraps the supplied
	 * connection.
	 */
	public SailConnectionWrapper(SailConnection delegate) {
		this.delegate = delegate;
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
	protected SailConnection getDelegate() {
		return delegate;
	}

	public boolean isOpen()
		throws StoreException
	{
		return getDelegate().isOpen();
	}

	public void close()
		throws StoreException
	{
		getDelegate().close();
	}

	public Isolation getTransactionIsolation()
		throws StoreException
	{
		return getDelegate().getTransactionIsolation();
	}

	public void setTransactionIsolation(Isolation isolation)
		throws StoreException
	{
		getDelegate().setTransactionIsolation(isolation);
	}

	public boolean isReadOnly()
		throws StoreException
	{
		return getDelegate().isReadOnly();
	}

	public void setReadOnly(boolean readOnly)
		throws StoreException
	{
		getDelegate().setReadOnly(readOnly);
	}

	public boolean isAutoCommit()
		throws StoreException
	{
		return getDelegate().isAutoCommit();
	}

	public void begin()
		throws StoreException
	{
		getDelegate().begin();
	}

	public void commit()
		throws StoreException
	{
		getDelegate().commit();
	}

	public void rollback()
		throws StoreException
	{
		getDelegate().rollback();
	}

	public ValueFactory getValueFactory() {
		return getDelegate().getValueFactory();
	}

	public Cursor<? extends BindingSet> evaluate(QueryModel query, BindingSet bindings, boolean includeInferred)
		throws StoreException
	{
		return getDelegate().evaluate(query, bindings, includeInferred);
	}

	public Cursor<? extends Resource> getContextIDs()
		throws StoreException
	{
		return getDelegate().getContextIDs();
	}

	public Cursor<? extends Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		return getDelegate().getStatements(subj, pred, obj, includeInferred, contexts);
	}

	public long size(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		return getDelegate().size(subj, pred, obj, includeInferred, contexts);
	}

	public void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		getDelegate().addStatement(subj, pred, obj, contexts);
	}

	public void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		getDelegate().removeStatements(subj, pred, obj, contexts);
	}

	public Cursor<? extends Namespace> getNamespaces()
		throws StoreException
	{
		return getDelegate().getNamespaces();
	}

	public String getNamespace(String prefix)
		throws StoreException
	{
		return getDelegate().getNamespace(prefix);
	}

	public void setNamespace(String prefix, String name)
		throws StoreException
	{
		getDelegate().setNamespace(prefix, name);
	}

	public void removeNamespace(String prefix)
		throws StoreException
	{
		getDelegate().removeNamespace(prefix);
	}

	public void clearNamespaces()
		throws StoreException
	{
		getDelegate().clearNamespaces();
	}
}
