/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.concurrent.locks.ExclusiveLockManager;
import info.aduna.concurrent.locks.Lock;
import info.aduna.concurrent.locks.ReadWriteLockManager;
import info.aduna.concurrent.locks.WritePrefReadWriteLockManager;
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

/**
 * Abstract Class offering base functionality for SailConnection
 * implementations.
 * 
 * @author Arjohn Kampman
 * @author jeen
 */
public abstract class SailConnectionBase implements SailConnection {

	/*
	 * Note: the following debugEnabled method are private so that they can be
	 * removed when open connections no longer block other connections and they
	 * can be closed silently (just like in JDBC).
	 */
	private static boolean debugEnabled() {
		try {
			return System.getProperty("org.openrdf.repository.debug") != null;
		}
		catch (SecurityException e) {
			// Thrown when not allowed to read system properties, for example
			// when running in applets
			return false;
		}
	}

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*-----------*
	 * Variables *
	 *-----------*/

	private final SailBase sailBase;

	private volatile boolean isOpen;

	private volatile boolean txnActive;

	/**
	 * A read-write lock manager used to handle concurrent access on the
	 * connection. Every operation on the connection must first obtain a shared
	 * (read) lock. When close() is invoked on this connection, the close()
	 * method will first obtain an exclusive (write) lock: it will wait until
	 * active operations finish and then block any further operations on the
	 * connection.
	 */
	private final ReadWriteLockManager connectionLockManager = new WritePrefReadWriteLockManager(
			debugEnabled());

	/**
	 * An exclusive lock manager used to prevent concurrent calls to update
	 * methods like addStatement, clear, commit, etc. within a transcation.
	 */
	private final ExclusiveLockManager updateLockManager = new ExclusiveLockManager(debugEnabled());

	// FIXME: use weak references here?
	private final List<SailBaseIteration> activeIterations = Collections.synchronizedList(new LinkedList<SailBaseIteration>());

	/*
	 * Stores a stack trace that indicates where this connection as created if
	 * debugging is enabled.
	 */
	private final Throwable creatorTrace;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SailConnectionBase(SailBase sailBase) {
		this.sailBase = sailBase;
		isOpen = true;
		txnActive = false;
		creatorTrace = debugEnabled() ? new Throwable() : null;
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
					while (true) {
						SailBaseIteration ci = null;

						synchronized (activeIterations) {
							if (activeIterations.isEmpty()) {
								break;
							}
							else {
								ci = activeIterations.remove(0);
							}
						}

						try {
							ci.forceClose();
						}
						catch (SailException e) {
							throw e;
						}
						catch (Exception e) {
							throw new SailException(e);
						}
					}

					assert activeIterations.isEmpty();

					if (txnActive) {
						logger.warn("Rolling back transaction due to connection close", new Throwable());
						try {
							// Use internal method to avoid deadlock: the public
							// rollback method will try to obtain a connection lock
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

	@Override
	protected void finalize()
		throws Throwable
	{
		try {
			if (isOpen()) {
				if (creatorTrace != null) {
					logger.warn("Closing connection due to garbage collection, connection was created in:",
							creatorTrace);
				}
				close();
			}
		}
		finally {
			super.finalize();
		}
	}

	public final CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(
			TupleExpr tupleExpr, Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();
			return registerIteration(evaluateInternal(tupleExpr, dataset, bindings, includeInferred));
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
			Value obj, boolean includeInferred, Resource... contexts)
		throws SailException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();
			return registerIteration(getStatementsInternal(subj, pred, obj, includeInferred, contexts));
		}
		finally {
			conLock.release();
		}
	}

	public final long size(Resource... contexts)
		throws SailException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();
			return sizeInternal(contexts);
		}
		finally {
			conLock.release();
		}
	}

	protected final boolean transactionActive() {
		return txnActive;
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

			Lock updateLock = getUpdateLock();
			try {
				if (txnActive) {
					commitInternal();
					txnActive = false;
				}
			}
			finally {
				updateLock.release();
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

			Lock updateLock = getUpdateLock();
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
				updateLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	public final void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();

			Lock updateLock = getUpdateLock();
			try {
				autoStartTransaction();
				addStatementInternal(subj, pred, obj, contexts);
			}
			finally {
				updateLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	public final void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();

			Lock updateLock = getUpdateLock();
			try {
				autoStartTransaction();
				removeStatementsInternal(subj, pred, obj, contexts);
			}
			finally {
				updateLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	public final void clear(Resource... contexts)
		throws SailException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();

			Lock updateLock = getUpdateLock();
			try {
				autoStartTransaction();
				clearInternal(contexts);
			}
			finally {
				updateLock.release();
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

			Lock updateLock = getUpdateLock();
			try {
				autoStartTransaction();
				setNamespaceInternal(prefix, name);
			}
			finally {
				updateLock.release();
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

			Lock updateLock = getUpdateLock();
			try {
				autoStartTransaction();
				removeNamespaceInternal(prefix);
			}
			finally {
				updateLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	public final void clearNamespaces()
		throws SailException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();

			Lock updateLock = getUpdateLock();
			try {
				autoStartTransaction();
				clearNamespacesInternal();
			}
			finally {
				updateLock.release();
			}
		}
		finally {
			conLock.release();
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

	@Deprecated
	protected Lock getTransactionLock()
		throws SailException
	{
		return getUpdateLock();
	}

	/**
	 * Gets the lock that prevents concurrent calls to update methods like
	 * addStatement, clear, commit, etc. All such methods must acquire this lock
	 * before doing anything else.
	 */
	protected Lock getUpdateLock()
		throws SailException
	{
		try {
			return updateLockManager.getExclusiveLock();
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

	protected abstract void closeInternal()
		throws SailException;

	protected abstract CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(
			TupleExpr tupleExpr, Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws SailException;

	protected abstract CloseableIteration<? extends Resource, SailException> getContextIDsInternal()
		throws SailException;

	protected abstract CloseableIteration<? extends Statement, SailException> getStatementsInternal(
			Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
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

	protected abstract void clearNamespacesInternal()
		throws SailException;
}
