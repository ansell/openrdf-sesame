/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.cursor.Cursor;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.sail.SailConnection;
import org.openrdf.store.ConnectionClosedException;
import org.openrdf.store.StoreException;

/**
 * A SailConnection that checks common preconditions for method calls. This
 * includes:
 * <ul>
 * <li>open/close status</li>
 * <li>read-only mode</li>
 * <li>transaction status (active/inactive)</li>
 * </ul>
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class PreconditionSailConnection extends SailConnectionWrapper {

	protected final Logger logger = LoggerFactory.getLogger(PreconditionSailConnection.class);

	/*--------------*
	 * Constructors *
	 *--------------*/

	public PreconditionSailConnection(SailConnection con) {
		super(con);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Verifies that the connection is open, throws a {@link StoreException} if
	 * it isn't.
	 */
	protected void verifyIsOpen()
		throws StoreException
	{
		if (!isOpen()) {
			throw new ConnectionClosedException();
		}
	}

	/**
	 * Verifies that the connection is not in read-only mode, throws a
	 * {@link StoreException} if it is.
	 */
	protected void verifyNotReadOnly()
		throws StoreException
	{
		if (isReadOnly()) {
			throw new StoreException("Connection is in read-only mode");
		}
	}

	/**
	 * Verifies that the connection has an active transaction, throws a
	 * {@link StoreException} if it hasn't.
	 */
	protected void verifyTxnActive()
		throws StoreException
	{
		if (isAutoCommit()) {
			throw new StoreException("Connection does not have an active transaction");
		}
	}

	/**
	 * Verifies that the connection does not have an active transaction, throws a
	 * {@link StoreException} if it has.
	 */
	protected void verifyNotTxnActive(String msg)
		throws StoreException
	{
		if (!isAutoCommit()) {
			throw new StoreException(msg);
		}
	}

	/**
	 * Checks whether the connection is still open and closes it if that is the
	 * case. The method call is ignored when the connection is already closed.
	 */
	@Override
	public final void close()
		throws StoreException
	{
		if (isOpen()) {
			super.close();
		}
	}

	@Override
	public void setReadOnly(boolean readOnly)
		throws StoreException
	{
		verifyNotTxnActive("read-only mode cannot be changed during a transaction");
		getDelegate().setReadOnly(readOnly);
	}

	@Override
	public final Cursor<? extends BindingSet> evaluate(QueryModel query, BindingSet bindings,
			boolean includeInferred)
		throws StoreException
	{
		verifyIsOpen();
		return super.evaluate(query, bindings, includeInferred);
	}

	@Override
	public final Cursor<? extends Resource> getContextIDs()
		throws StoreException
	{
		verifyIsOpen();
		return super.getContextIDs();
	}

	@Override
	public final Cursor<? extends Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		verifyIsOpen();
		return super.getStatements(subj, pred, obj, includeInferred, contexts);
	}

	@Override
	public final long size(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		verifyIsOpen();
		return super.size(subj, pred, obj, includeInferred, contexts);
	}

	@Override
	public void begin()
		throws StoreException
	{
		verifyIsOpen();
		verifyNotTxnActive("Connection already has an active transaction");
		super.begin();
	}

	@Override
	public final void commit()
		throws StoreException
	{
		verifyIsOpen();
		verifyTxnActive();
		super.commit();
	}

	@Override
	public final void rollback()
		throws StoreException
	{
		verifyIsOpen();
		verifyTxnActive();
		super.rollback();
	}

	@Override
	public final void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		verifyIsOpen();
		verifyNotReadOnly();
		super.addStatement(subj, pred, obj, contexts);
	}

	@Override
	public final void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		verifyIsOpen();
		verifyNotReadOnly();
		super.removeStatements(subj, pred, obj, contexts);
	}

	@Override
	public final Cursor<? extends Namespace> getNamespaces()
		throws StoreException
	{
		verifyIsOpen();
		return super.getNamespaces();
	}

	@Override
	public final String getNamespace(String prefix)
		throws StoreException
	{
		verifyIsOpen();
		return super.getNamespace(prefix);
	}

	@Override
	public final void setNamespace(String prefix, String name)
		throws StoreException
	{
		verifyIsOpen();
		verifyNotReadOnly();
		super.setNamespace(prefix, name);
	}

	@Override
	public final void removeNamespace(String prefix)
		throws StoreException
	{
		verifyIsOpen();
		verifyNotReadOnly();
		super.removeNamespace(prefix);
	}

	@Override
	public final void clearNamespaces()
		throws StoreException
	{
		verifyIsOpen();
		verifyNotReadOnly();
		super.clearNamespaces();
	}
}
