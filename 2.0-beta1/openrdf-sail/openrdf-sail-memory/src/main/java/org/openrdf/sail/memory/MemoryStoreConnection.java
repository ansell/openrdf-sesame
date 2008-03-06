/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory;

import info.aduna.concurrent.locks.Lock;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.FilterIteration;
import info.aduna.iteration.IteratorIteration;
import info.aduna.iteration.LockingIteration;
import info.aduna.iteration.UnionIteration;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.impl.BooleanExprOptimizer;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.algebra.evaluation.impl.QueryJoinOptimizer;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.sail.InferencerConnection;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionBase;
import org.openrdf.sail.memory.model.MemResource;
import org.openrdf.sail.memory.model.MemStatement;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.openrdf.sail.memory.model.ReadMode;
import org.openrdf.sail.memory.model.TxnStatus;

/**
 * Implementation of a Sail Connection for memory stores.
 * 
 * @author jeen
 */
public class MemoryStoreConnection extends SailConnectionBase implements InferencerConnection {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final MemoryStore _store;

	private Lock _txnLock;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MemoryStoreConnection(MemoryStore store) {
		super();
		_store = store;
	}

	/*---------*
	 * Methods *
	 *---------*/

	protected CloseableIteration<? extends BindingSet, QueryEvaluationException> _evaluate(
			TupleExpr tupleExpr, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		Lock queryLock = _store.getQueryReadLock();

		try {
			TripleSource tripleSource = new MemTripleSource(includeInferred);
			EvaluationStrategyImpl strategy = new EvaluationStrategyImpl(tripleSource);

			MemoryStoreCostComparator comparator = new MemoryStoreCostComparator(_store);
			QueryJoinOptimizer joinOptimizer = new QueryJoinOptimizer(comparator);
			joinOptimizer.optimize(tupleExpr, EmptyBindingSet.getInstance());

			// Note: the query model should not be changed based on the supplied
			// bindings, the same query model can later be used with different
			// variable bindings
			BooleanExprOptimizer booleanExprOptimizer = new BooleanExprOptimizer(strategy);
			booleanExprOptimizer.optimize(tupleExpr, EmptyBindingSet.getInstance());

			CloseableIteration<BindingSet, QueryEvaluationException> iter = null;
			try {
				iter = strategy.evaluate(tupleExpr, bindings);
			}
			catch (QueryEvaluationException e) {
				throw new SailException(e);
			}
			return new LockingIteration<BindingSet, QueryEvaluationException>(queryLock, iter);
		}
		catch (RuntimeException e) {
			queryLock.release();
			throw e;
		}
	}

	@Override
	protected void _close()
		throws SailException
	{
		/**
		 * FIXME the close method should make sure that any still-open read locks
		 * are released. This to avoid a deadlock when shutDown() is invoked and
		 * tries to obtain a write lock. This situation can occur when an iterator
		 * is not properly closed and not dereferenced, but the connection is
		 * closed and the repository shut down: <code>
		 *  Iterator foo = con.getStatements();
		 * 
		 *  con.close(); // iterator still open and referenced, still holds read lock.
		 *  repository.shutdown(); // deadlock.
		 *  </code>
		 */
	}

	protected CloseableIteration<? extends Resource, SailException> _getContextIDs()
		throws SailException
	{
		Lock queryLock = _store.getQueryReadLock();

		try {
			// Iterate over all MemURIs and MemBNodes
			@SuppressWarnings("unchecked")
			CloseableIteration<MemResource, SailException> iter = new UnionIteration<MemResource, SailException>(
					new IteratorIteration<MemResource, SailException>(
							_store.getValueFactory().getMemURIs().iterator()),
					new IteratorIteration<MemResource, SailException>(
							_store.getValueFactory().getMemBNodes().iterator()));

			// Only return the resources that are actually used as a context
			// identifier
			iter = new FilterIteration<MemResource, SailException>(iter) {

				protected boolean accept(MemResource memResource) {
					return memResource.getContextStatementCount() > 0;
				}
			};

			// Release query lock when iterator is closed
			iter = new LockingIteration<MemResource, SailException>(queryLock, iter);

			return iter;
		}
		catch (RuntimeException e) {
			queryLock.release();
			throw e;
		}
	}

	protected CloseableIteration<? extends Statement, SailException> _getStatements(Resource subj, URI pred,
			Value obj, boolean includeInferred, Resource... contexts)
		throws SailException
	{
		Lock queryLock = _store.getQueryReadLock();

		try {
			return new LockingIteration<MemStatement, SailException>(queryLock, _store.createStatementIterator(
					SailException.class, subj, pred, obj, !includeInferred, ReadMode.COMMITTED, contexts));
		}
		catch (RuntimeException e) {
			queryLock.release();
			throw e;
		}
	}

	protected long _size(Resource... contexts)
		throws SailException
	{
		long size = 0;
		Lock queryLock = _store.getQueryReadLock();
		try {
			if (contexts.length == 0) {
				size = _store.getStatements().size();
			}
			else {
				SailConnection con = _store.getConnection();
				CloseableIteration<? extends Statement, SailException> iter = con.getStatements(null, null, null,
						false, contexts);
				while (iter.hasNext()) {
					iter.next();
					size++;
				}
			}
		}
		catch (SailException e) {
			// TODO
		}
		finally {
			queryLock.release();
		}
		return size;
	}

	protected CloseableIteration<? extends Namespace, SailException> _getNamespaces()
		throws SailException
	{
		Lock queryLock = _store.getQueryReadLock();
		try {
			return new LockingIteration<Namespace, SailException>(queryLock,
					new IteratorIteration<Namespace, SailException>(_store.getNamespaceStore().iterator()));
		}
		catch (RuntimeException e) {
			queryLock.release();
			throw e;
		}
	}

	protected String _getNamespace(String prefix)
		throws SailException
	{
		Lock queryLock = _store.getQueryReadLock();
		try {
			return _store.getNamespaceStore().getNamespace(prefix);
		}
		finally {
			queryLock.release();
		}
	}

	protected void _startTransaction()
		throws SailException
	{
		if (!_store.isWritable()) {
			throw new SailException("Unable to start transaction: data file is read-only");
		}

		// Prevent concurrent transactions by acquiring an exclusive txn lock
		_txnLock = _store.getTransactionLock();
		_store.stopSyncTimer();
	}

	protected void _commit()
		throws SailException
	{
		// Prevent querying during commit:
		Lock queryLock = _store.getQueryWriteLock();

		try {
			_store.commit();
		}
		finally {
			queryLock.release();
		}

		_txnLock.release();
		_store.startSyncTimer();
	}

	protected void _rollback()
		throws SailException
	{
		try {
			_store.rollback();
		}
		finally {
			_txnLock.release();
		}
	}

	protected void _addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		_addStatement(subj, pred, obj, true, contexts);
	}

	public boolean addInferredStatement(Resource subj, URI pred, Value obj, Resource context)
		throws SailException
	{
		_autoStartTransaction();

		return _addStatement(subj, pred, obj, false, context);
	}

	/**
	 * Adds the specified statement to this MemoryStore.
	 * 
	 * @throws SailException
	 */
	private boolean _addStatement(Resource subj, URI pred, Value obj, boolean explicit, Resource... contexts)
		throws SailException
	{
		Statement st = null;
		if (contexts.length == 0) {
			st = _store.addStatement(subj, pred, obj, null, explicit);
			if (st != null) {
				_notifyStatementAdded(st);
			}
		}
		else {
			for (Resource context : contexts) {
				st = _store.addStatement(subj, pred, obj, context, explicit);
				if (st != null) {
					_notifyStatementAdded(st);
				}
			}
		}
		return st != null;
	}

	protected void _removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		_removeStatements(subj, pred, obj, true, contexts);
	}

	public boolean removeInferredStatement(Resource subj, URI pred, Value obj, Resource context)
		throws SailException
	{
		_autoStartTransaction();

		return _removeStatements(subj, pred, obj, false, context);
	}

	protected void _clearContext(Resource context)
		throws SailException
	{
		_removeStatements(null, null, null, true, context);
	}

	protected void _clear(Resource... contexts)
		throws SailException
	{
		_removeStatements(null, null, null, true, contexts);
	}

	public void clearInferredFromContext(Resource context)
		throws SailException
	{
		_autoStartTransaction();

		_removeStatements(null, null, null, false, context);
	}

	public void clearInferred()
		throws SailException
	{
		_autoStartTransaction();

		_removeStatements(null, null, null, false);
	}

	/**
	 * Removes the statements that match the specified pattern of subject,
	 * predicate, object and context.
	 * 
	 * @param subj
	 *        The subject for the pattern, or <tt>null</tt> for a wildcard.
	 * @param pred
	 *        The predicate for the pattern, or <tt>null</tt> for a wildcard.
	 * @param obj
	 *        The object for the pattern, or <tt>null</tt> for a wildcard.
	 * @param explicit
	 *        Flag indicating whether explicit or inferred statements should be
	 *        removed; <tt>true</tt> removes explicit statements that match the
	 *        pattern, <tt>false</tt> removes inferred statements that match
	 *        the pattern.
	 * @throws SailException
	 */
	private boolean _removeStatements(Resource subj, URI pred, Value obj, boolean explicit,
			Resource... contexts)
		throws SailException
	{
		CloseableIteration<MemStatement, SailException> stIter = _store.createStatementIterator(
				SailException.class, subj, pred, obj, explicit, ReadMode.TRANSACTION, contexts);

		return _removeIteratorStatements(stIter, explicit);
	}

	private <X extends Exception> boolean _removeIteratorStatements(
			CloseableIteration<MemStatement, X> stIter, boolean explicit)
		throws X
	{
		boolean statementsRemoved = false;

		try {
			while (stIter.hasNext()) {
				MemStatement st = stIter.next();

				// update the statement's transaction status
				TxnStatus txnStatus = st.getTxnStatus();

				if (txnStatus == TxnStatus.NEUTRAL && st.isExplicit() == explicit) {
					st.setTxnStatus(TxnStatus.DEPRECATED);
					_notifyStatementRemoved(st);
					statementsRemoved = true;
				}
				else if (txnStatus == TxnStatus.NEW && st.isExplicit() == explicit) {
					st.setTxnStatus(TxnStatus.ZOMBIE);
					_notifyStatementRemoved(st);
					statementsRemoved = true;
				}
				else if (txnStatus == TxnStatus.EXPLICIT && !st.isExplicit() && explicit) {
					st.setTxnStatus(TxnStatus.NEUTRAL);
				}
				else if (txnStatus == TxnStatus.INFERRED && st.isExplicit() && !explicit) {
					st.setTxnStatus(TxnStatus.DEPRECATED);
					_notifyStatementRemoved(st);
					statementsRemoved = true;
				}
			}
		}
		finally {
			stIter.close();
		}

		return statementsRemoved;

	}

	protected void _setNamespace(String prefix, String name)
		throws SailException
	{
		// FIXME: changes to namespace prefixes not isolated in transactions yet
		try {
			_store.getNamespaceStore().setNamespace(prefix, name);
		}
		catch (IllegalArgumentException e) {
			throw new SailException(e.getMessage());
		}
	}

	protected void _removeNamespace(String prefix)
		throws SailException
	{
		_store.getNamespaceStore().removeNamespace(prefix);
	}

	/*-----------------------------*
	 * Inner class MemTripleSource *
	 *-----------------------------*/

	/**
	 * Implementation of the TripleSource interface from the Sail Query Model
	 */
	class MemTripleSource implements TripleSource {

		private boolean _includeInferred;

		public MemTripleSource(boolean includeInferred) {
			_includeInferred = includeInferred;
		}

		public CloseableIteration<MemStatement, QueryEvaluationException> getStatements(Resource subj,
				URI pred, Value obj, Resource... contexts)
		{
			return _store.createStatementIterator(QueryEvaluationException.class, subj, pred, obj,
					!_includeInferred, ReadMode.COMMITTED, contexts);
		}

		public MemValueFactory getValueFactory() {
			return _store.getValueFactory();
		}
	} // end inner class MemTripleSource
}
