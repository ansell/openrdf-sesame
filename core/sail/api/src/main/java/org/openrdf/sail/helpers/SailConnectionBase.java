/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Clear;
import org.openrdf.query.algebra.DeleteData;
import org.openrdf.query.algebra.InsertData;
import org.openrdf.query.algebra.Load;
import org.openrdf.query.algebra.Modify;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
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
	 * Lock used to give the {@link #close()} method exclusive access to a
	 * connection.
	 * <ul>
	 * <li>write lock: close()
	 * <li>read lock: all other (public) methods
	 * </ul>
	 */
	protected final ReentrantReadWriteLock connectionLock = new ReentrantReadWriteLock();

	/**
	 * Lock used to prevent concurrent calls to update methods like addStatement,
	 * clear, commit, etc. within a transaction.
	 */
	protected final ReentrantLock updateLock = new ReentrantLock();

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
		connectionLock.writeLock().lock();

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
			connectionLock.writeLock().unlock();
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
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();
			return registerIteration(evaluateInternal(tupleExpr, dataset, bindings, includeInferred));
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	public final void executeUpdate(UpdateExpr updateExpr, Dataset dataset, BindingSet bindings,
			boolean includeInferred)
		throws SailException
	{
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();

			updateLock.lock();
			try {
				autoStartTransaction();
				executeInternal(updateExpr, dataset, bindings, includeInferred);
			}
			finally {
				updateLock.unlock();
			}
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	public final CloseableIteration<? extends Resource, SailException> getContextIDs()
		throws SailException
	{
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();
			return registerIteration(getContextIDsInternal());
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	public final CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, URI pred,
			Value obj, boolean includeInferred, Resource... contexts)
		throws SailException
	{
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();
			return registerIteration(getStatementsInternal(subj, pred, obj, includeInferred, contexts));
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	public final long size(Resource... contexts)
		throws SailException
	{
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();
			return sizeInternal(contexts);
		}
		finally {
			connectionLock.readLock().unlock();
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
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();

			updateLock.lock();
			try {
				if (txnActive) {
					commitInternal();
					txnActive = false;
				}
			}
			finally {
				updateLock.unlock();
			}
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	public final void rollback()
		throws SailException
	{
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();

			updateLock.lock();
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
				updateLock.unlock();
			}
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	public final void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();

			updateLock.lock();
			try {
				autoStartTransaction();
				addStatementInternal(subj, pred, obj, contexts);
			}
			finally {
				updateLock.unlock();
			}
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	public final void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();

			updateLock.lock();
			try {
				autoStartTransaction();
				removeStatementsInternal(subj, pred, obj, contexts);
			}
			finally {
				updateLock.unlock();
			}
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	public final void clear(Resource... contexts)
		throws SailException
	{
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();

			updateLock.lock();
			try {
				autoStartTransaction();
				clearInternal(contexts);
			}
			finally {
				updateLock.unlock();
			}
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	public final CloseableIteration<? extends Namespace, SailException> getNamespaces()
		throws SailException
	{
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();
			return registerIteration(getNamespacesInternal());
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	public final String getNamespace(String prefix)
		throws SailException
	{
		if (prefix == null) {
			throw new NullPointerException("prefix must not be null");
		}
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();
			return getNamespaceInternal(prefix);
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	public final void setNamespace(String prefix, String name)
		throws SailException
	{
		if (prefix == null) {
			throw new NullPointerException("prefix must not be null");
		}
		if (name == null) {
			throw new NullPointerException("name must not be null");
		}
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();

			updateLock.lock();
			try {
				autoStartTransaction();
				setNamespaceInternal(prefix, name);
			}
			finally {
				updateLock.unlock();
			}
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	public final void removeNamespace(String prefix)
		throws SailException
	{
		if (prefix == null) {
			throw new NullPointerException("prefix must not be null");
		}
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();

			updateLock.lock();
			try {
				autoStartTransaction();
				removeNamespaceInternal(prefix);
			}
			finally {
				updateLock.unlock();
			}
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	public final void clearNamespaces()
		throws SailException
	{
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();

			updateLock.lock();
			try {
				autoStartTransaction();
				clearNamespacesInternal();
			}
			finally {
				updateLock.unlock();
			}
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	/**
	 * @deprecated Use {@link #connectionLock} directly instead.
	 */
	protected info.aduna.concurrent.locks.Lock getSharedConnectionLock()
		throws SailException
	{
		return new JavaLock(connectionLock.readLock());
	}

	/**
	 * @deprecated Use {@link #connectionLock} directly instead.
	 */
	protected info.aduna.concurrent.locks.Lock getExclusiveConnectionLock()
		throws SailException
	{
		return new JavaLock(connectionLock.writeLock());
	}

	/**
	 * @deprecated Use {@link #updateLock} directly instead.
	 */
	@Deprecated
	protected info.aduna.concurrent.locks.Lock getTransactionLock()
		throws SailException
	{
		return new JavaLock(updateLock);
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

	protected void executeInternal(UpdateExpr updateExpr, Dataset dataset, BindingSet bindings,
			boolean includeInferred)
		throws SailException
	{
		/* TODO this method should really be defined abstract, but for backward-compatibility 
		 * purposes with third-party SAIL implementation we provide a default implementation for 
		 * now.
		 */
		logger.trace("Incoming update expression:\n{}", updateExpr);

		if (updateExpr instanceof Modify) {
			executeModify((Modify)updateExpr, dataset, bindings, includeInferred);
		}
		else if (updateExpr instanceof InsertData) {
			executeInsertData((InsertData)updateExpr, dataset, bindings, includeInferred);
		}
		else if (updateExpr instanceof DeleteData) {
			executeDeleteData((DeleteData)updateExpr, dataset, bindings, includeInferred);
		}
		else if (updateExpr instanceof Clear) {
			executeClear((Clear)updateExpr, dataset, bindings, includeInferred);
		}
		else if (updateExpr instanceof Load) {
			throw new SailException("load operations can not be handled directly by the SAIL");
		}
	}

	
	/**
	 * @param updateExpr
	 * @param dataset
	 * @param bindings
	 * @param includeInferred
	 * @throws SailException 
	 */
	protected void executeClear(Clear clearExpr, Dataset dataset, BindingSet bindings, boolean includeInferred) throws SailException 
	{
		try {
			ValueConstant graph = clearExpr.getGraph();
			
			if (graph != null) {
				Resource context = (Resource)graph.getValue();
				clearInternal(context);
			}
			else {
				Scope scope = clearExpr.getScope();
				if (Scope.NAMED_CONTEXTS.equals(scope)) {
					CloseableIteration<? extends Resource, SailException> contextIDs = getContextIDsInternal();
					while (contextIDs.hasNext()) {
						clearInternal(contextIDs.next());
					}
				}
				else if (Scope.DEFAULT_CONTEXTS.equals(scope)) {
					clearInternal((Resource)null);
				}
				else {
					clearInternal();
				}
			}
		}
		catch (SailException e) {
			if (!clearExpr.isSilent()) {
				throw e;
			}
		}
	}

	/**
	 * @param updateExpr
	 * @param dataset
	 * @param bindings
	 * @param includeInferred
	 * @throws SailException 
	 */
	protected void executeInsertData(InsertData insertDataExpr, Dataset dataset, BindingSet bindings,
			boolean includeInferred) throws SailException
	{
		TupleExpr insertExpr = insertDataExpr.getInsertExpr();

		CloseableIteration<? extends BindingSet, QueryEvaluationException> toBeInserted = evaluateInternal(
				insertExpr, dataset, bindings, includeInferred);
		
		try {
			while (toBeInserted.hasNext()) {
				BindingSet bs = toBeInserted.next();
				
				Resource subject = (Resource)bs.getValue("subject");
				URI predicate = (URI)bs.getValue("predicate");
				Value object = bs.getValue("object");
				Resource context = (Resource)bs.getValue("context");
				
				if (context == null) {
					addStatementInternal(subject, predicate, object);
				}
				else {
					addStatementInternal(subject, predicate, object, context);
				}
			}
		}
		catch (QueryEvaluationException e) {
			throw new SailException(e);
		}
	}

	/**
	 * @param updateExpr
	 * @param dataset
	 * @param bindings
	 * @param includeInferred
	 * @throws SailException 
	 */
	protected void executeDeleteData(DeleteData deleteDataExpr, Dataset dataset, BindingSet bindings,
			boolean includeInferred) throws SailException
	{
		TupleExpr deleteExpr = deleteDataExpr.getDeleteExpr();

		CloseableIteration<? extends BindingSet, QueryEvaluationException> toBeDeleted = evaluateInternal(
				deleteExpr, dataset, bindings, includeInferred);
		
		try {
			while (toBeDeleted.hasNext()) {
				BindingSet bs = toBeDeleted.next();
				
				Resource subject = (Resource)bs.getValue("subject");
				URI predicate = (URI)bs.getValue("predicate");
				Value object = bs.getValue("object");
				Resource context = (Resource)bs.getValue("context");
				
				if (context == null) {
					removeStatementsInternal(subject, predicate, object);
				}
				else {
					removeStatementsInternal(subject, predicate, object, context);
				}
			}
		}
		catch (QueryEvaluationException e) {
			throw new SailException(e);
		}
	}

	protected void executeModify(Modify modify, Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		TupleExpr deleteClause = modify.getDeleteExpr();
		TupleExpr insertClause = modify.getInsertExpr();
		TupleExpr whereClause = modify.getWhereExpr();

		CloseableIteration<? extends BindingSet, QueryEvaluationException> sourceBindings = evaluateInternal(
				whereClause, dataset, bindings, includeInferred);

		try {
			while (sourceBindings.hasNext()) {
				BindingSet sourceBinding = sourceBindings.next();

				if (deleteClause != null) {
					List<StatementPattern> deletePatterns = StatementPatternCollector.process(deleteClause);

					for (StatementPattern deletePattern : deletePatterns) {
						Statement toBeDeleted = createStatementFromPattern(deletePattern, sourceBinding, false);

						if (toBeDeleted.getContext() == null) {
							removeStatementsInternal(toBeDeleted.getSubject(), toBeDeleted.getPredicate(),
									toBeDeleted.getObject());
						}
						else {
							removeStatementsInternal(toBeDeleted.getSubject(), toBeDeleted.getPredicate(),
									toBeDeleted.getObject(), toBeDeleted.getContext());
						}
					}
				}

				if (insertClause != null) {
					List<StatementPattern> insertPatterns = StatementPatternCollector.process(insertClause);

					for (StatementPattern insertPattern : insertPatterns) {
						Statement toBeInserted = createStatementFromPattern(insertPattern, sourceBinding, true);

						if (toBeInserted.getContext() == null) {
							addStatementInternal(toBeInserted.getSubject(), toBeInserted.getPredicate(),
									toBeInserted.getObject());
						}
						else {
							addStatementInternal(toBeInserted.getSubject(), toBeInserted.getPredicate(),
									toBeInserted.getObject(), toBeInserted.getContext());
						}
					}
				}
			}
		}
		catch (QueryEvaluationException e) {
			throw new SailException(e);
		}
	}

	/**
	 * @param deletePattern
	 * @param sourceBinding
	 * @return
	 * @throws SailException
	 */
	private Statement createStatementFromPattern(StatementPattern deletePattern, BindingSet sourceBinding,
			boolean forceBindAll)
		throws SailException
	{

		Resource subject = null;
		URI predicate = null;
		Value object = null;
		Resource context = null;

		ValueFactory f = sailBase.getValueFactory();

		if (deletePattern.getSubjectVar().hasValue()) {
			subject = (Resource)deletePattern.getSubjectVar().getValue();
		}
		else {
			subject = (Resource)sourceBinding.getValue(deletePattern.getSubjectVar().getName());
			if (forceBindAll && subject == null) {
				subject = f.createBNode();
			}
		}

		if (deletePattern.getPredicateVar().hasValue()) {
			predicate = (URI)deletePattern.getPredicateVar().getValue();
		}
		else {
			predicate = (URI)sourceBinding.getValue(deletePattern.getPredicateVar().getName());
			if (forceBindAll && predicate == null) {
				throw new SailException("cannot instantiate predicate binding for statement pattern");
			}
		}

		if (deletePattern.getObjectVar().hasValue()) {
			object = deletePattern.getObjectVar().getValue();
		}
		else {
			object = sourceBinding.getValue(deletePattern.getObjectVar().getName());
			if (forceBindAll && object == null) {
				object = f.createBNode();
			}
		}

		if (deletePattern.getContextVar() != null) {
			if (deletePattern.getContextVar().hasValue()) {
				context = (Resource)deletePattern.getContextVar().getValue();
			}
			else {
				context = (Resource)sourceBinding.getValue(deletePattern.getContextVar().getName());
			}
		}

		Statement st = null;
		if (context != null) {
			st = f.createStatement(subject, predicate, object, context);
		}
		else {
			st = f.createStatement(subject, predicate, object);
		}
		return st;
	}

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

	private static class JavaLock implements info.aduna.concurrent.locks.Lock {

		private final Lock javaLock;

		private boolean isActive = true;

		public JavaLock(Lock javaLock) {
			this.javaLock = javaLock;
			javaLock.lock();
		}

		public synchronized boolean isActive() {
			return isActive;
		}

		public synchronized void release() {
			if (isActive) {
				javaLock.unlock();
				isActive = false;
			}
		}
	}
}
