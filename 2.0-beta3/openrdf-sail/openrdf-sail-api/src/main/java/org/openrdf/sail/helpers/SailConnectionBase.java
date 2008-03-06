/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.concurrent.locks.ExclusiveLockManager;
import info.aduna.concurrent.locks.Lock;
import info.aduna.concurrent.locks.MultiReadSingleWriteLockManager;
import info.aduna.iteration.CloseableIteration;
import info.aduna.lang.VarargsNullValueException;

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
 * Abstract Class offering base functionality for SailConnection
 * implementations.
 * 
 * @author Arjohn Kampman
 * @author jeen
 */
public abstract class SailConnectionBase implements SailConnection {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*-----------*
	 * Variables *
	 *-----------*/

	private final SailBase sailBase;

	private boolean isOpen;

	private boolean txnActive;

	/**
	 * A multi-read-single-write lock manager used to handle multi-threaded
	 * access on the connection. Every operation on the connection must first
	 * obtain a shared (read) lock. When close() is invoked on this connection,
	 * the close() method will first obtain an exclusive (write) lock: it will
	 * wait until active operations finish and then block any further operations
	 * on the connection.
	 */
	private final MultiReadSingleWriteLockManager connectionLockManager = new MultiReadSingleWriteLockManager();

	/**
	 * A multi-read-single-write lock manager used to handle multi-threaded
	 * access on the transaction-related methods of a connection. Every
	 * transaction operation except commit and rollback must first obtain a
	 * shared (read) lock. The commit and rollback themselves will first obtain
	 * an exclusive (write) lock, which will guarantee that there will be no
	 * updates during these operations.
	 */
	private final ExclusiveLockManager txnLockManager = new ExclusiveLockManager();

	private Set<CloseableIteration> activeIterations = new HashSet<CloseableIteration>();

	private List<SailConnectionListener> listeners;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SailConnectionBase(SailBase sailBase) {
		this.sailBase = sailBase;
		isOpen = true;
		txnActive = false;
		listeners = new ArrayList<SailConnectionListener>(0);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public final boolean isOpen()
		throws SailException
	{
		return isOpen;
	}

	protected void verifyIsOpen()
		throws SailException
	{
		if (!isOpen) {
			throw new IllegalStateException("Connection has been closed");
		}
	}

	public final void close()
		throws SailException
	{
		// obtain an exclusive lock so that any further operations on this
		// connection (including those from any concurrent threads) are blocked.
		Lock conLock = getExclusiveConnectionLock();

		try {
			if (isOpen) {
				try {
					Iterator<CloseableIteration> iter = activeIterations.iterator();
					if (iter.hasNext()) {
						logger.warn("Closing active iterations due to connection close...");
					}
					while (iter.hasNext()) {
						CloseableIteration ci = iter.next();

						// Remove from set so that iterationClosed(...) doesn't cause
						// ConcurrentModificationExceptions
						iter.remove();

						try {
							ci.close();
						}
						catch (SailException e) {
							throw (SailException)e;
						}
						catch (Exception e) {
							throw new SailException(e);
						}
					}

					assert activeIterations.isEmpty();

					if (txnActive) {
						logger.warn("Rolling back transaction due to connection close");
						try {
							// Use internal method to avoid deadlock: the public
							// rollback method will try to obtain a thread lock
							rollbackInternal();
						}
						finally {
							txnActive = false;
						}
					}

					closeInternal();
				}
				finally {
					isOpen = false;
					sailBase.connectionClosed(this);
				}
			}
		}
		finally {
			// Release the exclusive lock. Any threads waiting to obtain a
			// non-exclusive read lock will get one and then fail with an
			// IllegalStateException, because the connection is no longer open.
			conLock.release();
		}
	}

	public final CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(
			TupleExpr tupleExpr, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();
			return registerIteration(evaluateInternal(tupleExpr, bindings, includeInferred));
		}
		finally {
			conLock.release();
		}
	}

	public final CloseableIteration<? extends Resource, SailException> getContextIDs()
		throws SailException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();
			return registerIteration(getContextIDsInternal());
		}
		finally {
			conLock.release();
		}
	}

	public final CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, URI pred,
			Value obj, boolean useInference, Resource... contexts)
		throws SailException
	{
		verifyContextNotNull(contexts);
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();
			return registerIteration(getStatementsInternal(subj, pred, obj, useInference, contexts));
		}
		finally {
			conLock.release();
		}
	}

	public final long size(Resource... contexts)
		throws SailException
	{
		verifyContextNotNull(contexts);
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();
			return sizeInternal(contexts);
		}
		finally {
			conLock.release();
		}
	}

	protected void autoStartTransaction()
		throws SailException
	{
		if (!txnActive) {
			startTransactionInternal();
			txnActive = true;
		}
	}

	public final void commit()
		throws SailException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();

			Lock txnLock = getTransactionLock();
			try {
				if (txnActive) {
					commitInternal();
					txnActive = false;
				}
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	public final void rollback()
		throws SailException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();

			Lock txnLock = getTransactionLock();
			try {
				if (txnActive) {
					try {
						rollbackInternal();
					}
					finally {
						txnActive = false;
					}
				}
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	public final void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		verifyContextNotNull(contexts);

		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();

			Lock txnLock = getTransactionLock();
			try {
				autoStartTransaction();
				addStatementInternal(subj, pred, obj, contexts);
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	public final void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		verifyContextNotNull(contexts);

		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();

			Lock txnLock = getTransactionLock();
			try {
				autoStartTransaction();
				removeStatementsInternal(subj, pred, obj, contexts);
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	public final void clear(Resource... contexts)
		throws SailException
	{
		verifyContextNotNull(contexts);

		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();

			Lock txnLock = getTransactionLock();
			try {
				autoStartTransaction();
				clearInternal(contexts);
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	public final CloseableIteration<? extends Namespace, SailException> getNamespaces()
		throws SailException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();
			return registerIteration(getNamespacesInternal());
		}
		finally {
			conLock.release();
		}
	}

	public final String getNamespace(String prefix)
		throws SailException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();
			return getNamespaceInternal(prefix);
		}
		finally {
			conLock.release();
		}
	}

	public final void setNamespace(String prefix, String name)
		throws SailException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();

			Lock txnLock = getTransactionLock();
			try {
				autoStartTransaction();
				setNamespaceInternal(prefix, name);
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	public final void removeNamespace(String prefix)
		throws SailException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();

			Lock txnLock = getTransactionLock();
			try {
				autoStartTransaction();
				removeNamespaceInternal(prefix);
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	public void addConnectionListener(SailConnectionListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void removeConnectionListener(SailConnectionListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	protected boolean hasConnectionListeners() {
		synchronized (listeners) {
			return !listeners.isEmpty();
		}
	}

	protected void notifyStatementAdded(Statement st) {
		synchronized (listeners) {
			for (SailConnectionListener listener : listeners) {
				listener.statementAdded(st);
			}
		}
	}

	protected void notifyStatementRemoved(Statement st) {
		synchronized (listeners) {
			for (SailConnectionListener listener : listeners) {
				listener.statementRemoved(st);
			}
		}
	}

	protected Lock getSharedConnectionLock()
		throws SailException
	{
		try {
			return connectionLockManager.getReadLock();
		}
		catch (InterruptedException e) {
			throw new SailException(e);
		}
	}

	protected Lock getExclusiveConnectionLock()
		throws SailException
	{
		try {
			return connectionLockManager.getWriteLock();
		}
		catch (InterruptedException e) {
			throw new SailException(e);
		}
	}

	protected Lock getTransactionLock()
		throws SailException
	{
		try {
			return txnLockManager.getExclusiveLock();
		}
		catch (InterruptedException e) {
			throw new SailException(e);
		}
	}

	/**
	 * Registers an iteration as active by wrapping it in a
	 * {@link SailBaseIteration} object and adding it to the list of active
	 * iterations.
	 */
	protected <T, E extends Exception> CloseableIteration<T, E> registerIteration(CloseableIteration<T, E> iter)
	{
		SailBaseIteration<T, E> result = new SailBaseIteration<T, E>(iter, this);
		activeIterations.add(result);
		return result;
	}

	/**
	 * Called by {@link SailBaseIteration} to indicate that it has been closed.
	 */
	protected void iterationClosed(SailBaseIteration iter) {
		activeIterations.remove(iter);
	}

	protected void verifyContextNotNull(Resource... contexts)
		throws VarargsNullValueException
	{
		if (contexts == null) {
			throw new VarargsNullValueException();
		}
	}

	protected abstract void closeInternal()
		throws SailException;

	protected abstract CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(
			TupleExpr tupleExpr, BindingSet bindings, boolean includeInferred)
		throws SailException;

	protected abstract CloseableIteration<? extends Resource, SailException> getContextIDsInternal()
		throws SailException;

	protected abstract CloseableIteration<? extends Statement, SailException> getStatementsInternal(
			Resource subj, URI pred, Value obj, boolean useInference, Resource... contexts)
		throws SailException;

	protected abstract long sizeInternal(Resource... contexts)
		throws SailException;

	protected abstract void startTransactionInternal()
		throws SailException;

	protected abstract void commitInternal()
		throws SailException;

	protected abstract void rollbackInternal()
		throws SailException;

	protected abstract void addStatementInternal(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException;

	protected abstract void removeStatementsInternal(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException;

	protected abstract void clearInternal(Resource... contexts)
		throws SailException;

	protected abstract CloseableIteration<? extends Namespace, SailException> getNamespacesInternal()
		throws SailException;

	protected abstract String getNamespaceInternal(String prefix)
		throws SailException;

	protected abstract void setNamespaceInternal(String prefix, String name)
		throws SailException;

	protected abstract void removeNamespaceInternal(String prefix)
		throws SailException;
}
