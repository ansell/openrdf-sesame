/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import info.aduna.concurrent.locks.ExclusiveLockManager;
import info.aduna.concurrent.locks.Lock;
import info.aduna.concurrent.locks.ReadWriteLockManager;
import info.aduna.concurrent.locks.WritePrefReadWriteLockManager;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Cursor;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.SailConnection;
import org.openrdf.store.StoreException;

/**
 * Wrapper class offering synchronization functionality for SailConnection
 * implementations. This wrapper enforces two things:
 * <ul>
 * <li>Calls to {@link #close()} are exclusive. No other thread can call this
 * method, nor any other method at the same time.
 * <li>Calls to update-related methods (add, remove, commit, etc.) are
 * exclusive. These methods cannot be called simultaneously by multiple threads.
 * 
 * @author Arjohn Kampman
 * @author jeen
 * @author James Leigh
 */
public class SynchronizedSailConnection extends SailConnectionWrapper {

	/**
	 * A read-write lock manager used to handle multi-threaded access on the
	 * connection. Every operation on the connection must first obtain a shared
	 * (read) lock. When close() is invoked on this connection, the close()
	 * method will first obtain an exclusive (write) lock: it will wait until
	 * active operations finish and then block any further operations on the
	 * connection.
	 */
	private final ReadWriteLockManager connectionLockManager = new WritePrefReadWriteLockManager();

	/**
	 * A multi-read-single-write lock manager used to handle multi-threaded
	 * access on the transaction-related methods of a connection. Every
	 * transaction operation except commit and rollback must first obtain a
	 * shared (read) lock. The commit and rollback themselves will first obtain
	 * an exclusive (write) lock, which will guarantee that there will be no
	 * updates during these operations.
	 */
	private final ExclusiveLockManager txnLockManager = new ExclusiveLockManager();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SynchronizedSailConnection(SailConnection wrappedCon) {
		super(wrappedCon);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void close()
		throws StoreException
	{
		// obtain an exclusive lock so that any further operations on this
		// connection (including those from any concurrent threads) are blocked.
		Lock conLock = getExclusiveConnectionLock();

		try {
			super.close();
		}
		finally {
			// Release the exclusive lock. Any threads waiting to obtain a
			// non-exclusive read lock will get one and then fail with an
			// IllegalStateException, because the connection is no longer open.
			conLock.release();
		}
	}

	@Override
	public Cursor<? extends BindingSet> evaluate(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings,
			boolean includeInferred)
		throws StoreException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			return super.evaluate(tupleExpr, dataset, bindings, includeInferred);
		}
		finally {
			conLock.release();
		}
	}

	@Override
	public Cursor<? extends Resource> getContextIDs()
		throws StoreException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			return super.getContextIDs();
		}
		finally {
			conLock.release();
		}
	}

	@Override
	public Cursor<? extends Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			return super.getStatements(subj, pred, obj, includeInferred, contexts);
		}
		finally {
			conLock.release();
		}
	}

	@Override
	public long size(Resource... contexts)
		throws StoreException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			return super.size(contexts);
		}
		finally {
			conLock.release();
		}
	}

	@Override
	public void commit()
		throws StoreException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			Lock txnLock = getTransactionLock();
			try {
				super.commit();
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	@Override
	public void rollback()
		throws StoreException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			Lock txnLock = getTransactionLock();
			try {
				super.rollback();
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	@Override
	public void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			Lock txnLock = getTransactionLock();
			try {
				super.addStatement(subj, pred, obj, contexts);
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	@Override
	public void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			Lock txnLock = getTransactionLock();
			try {
				super.removeStatements(subj, pred, obj, contexts);
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	@Override
	public Cursor<? extends Namespace> getNamespaces()
		throws StoreException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			return super.getNamespaces();
		}
		finally {
			conLock.release();
		}
	}

	@Override
	public String getNamespace(String prefix)
		throws StoreException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			return super.getNamespace(prefix);
		}
		finally {
			conLock.release();
		}
	}

	@Override
	public void setNamespace(String prefix, String name)
		throws StoreException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			Lock txnLock = getTransactionLock();
			try {
				super.setNamespace(prefix, name);
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	@Override
	public void removeNamespace(String prefix)
		throws StoreException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			Lock txnLock = getTransactionLock();
			try {
				super.removeNamespace(prefix);
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	@Override
	public void clearNamespaces()
		throws StoreException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			Lock txnLock = getTransactionLock();
			try {
				super.clearNamespaces();
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	protected Lock getSharedConnectionLock()
		throws StoreException
	{
		try {
			return connectionLockManager.getReadLock();
		}
		catch (InterruptedException e) {
			throw new StoreException(e);
		}
	}

	protected Lock getExclusiveConnectionLock()
		throws StoreException
	{
		try {
			return connectionLockManager.getWriteLock();
		}
		catch (InterruptedException e) {
			throw new StoreException(e);
		}
	}

	protected Lock getTransactionLock()
		throws StoreException
	{
		try {
			return txnLockManager.getExclusiveLock();
		}
		catch (InterruptedException e) {
			throw new StoreException(e);
		}
	}
}
