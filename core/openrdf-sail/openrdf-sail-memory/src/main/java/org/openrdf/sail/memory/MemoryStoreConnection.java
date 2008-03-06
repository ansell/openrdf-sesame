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

import org.openrdf.OpenRDFUtil;
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
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionBase;
import org.openrdf.sail.inferencer.InferencerConnection;
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

	private final MemoryStore store;

	private Lock txnLock;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MemoryStoreConnection(MemoryStore store) {
		super(store);
		this.store = store;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(
			TupleExpr tupleExpr, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		Lock queryLock = store.getQueryReadLock();

		try {
			TripleSource tripleSource = new MemTripleSource(includeInferred);
			EvaluationStrategyImpl strategy = new EvaluationStrategyImpl(tripleSource);

			MemoryStoreCostComparator comparator = new MemoryStoreCostComparator(store);
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
	protected void closeInternal()
		throws SailException
	{
		// do nothing
	}

	@Override
	protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal()
		throws SailException
	{
		Lock queryLock = store.getQueryReadLock();

		try {
			// Iterate over all MemURIs and MemBNodes
			@SuppressWarnings("unchecked")
			CloseableIteration<MemResource, SailException> iter = new UnionIteration<MemResource, SailException>(
					new IteratorIteration<MemResource, SailException>(
							store.getValueFactory().getMemURIs().iterator()),
					new IteratorIteration<MemResource, SailException>(
							store.getValueFactory().getMemBNodes().iterator()));

			// Only return the resources that are actually used as a context
			// identifier
			iter = new FilterIteration<MemResource, SailException>(iter) {

				@Override
				protected boolean accept(MemResource memResource)
				{
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

	@Override
	protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(Resource subj,
			URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws SailException
	{
		Lock queryLock = store.getQueryReadLock();

		try {
			return new LockingIteration<MemStatement, SailException>(queryLock, store.createStatementIterator(
					SailException.class, subj, pred, obj, !includeInferred, ReadMode.COMMITTED, contexts));
		}
		catch (RuntimeException e) {
			queryLock.release();
			throw e;
		}
	}

	@Override
	protected long sizeInternal(Resource... contexts)
		throws SailException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		long size = 0;
		Lock queryLock = store.getQueryReadLock();
		try {
			if (contexts.length == 0) {
				size = store.getStatements().size();
			}
			else {
				SailConnection con = store.getConnection();
				try {
					CloseableIteration<? extends Statement, SailException> iter = con.getStatements(null, null,
							null, false, contexts);
					while (iter.hasNext()) {
						iter.next();
						size++;
					}
					iter.close();
				}
				finally {
					con.close();
				}
			}
		}
		finally {
			queryLock.release();
		}
		return size;
	}

	@Override
	protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal()
		throws SailException
	{
		Lock queryLock = store.getQueryReadLock();
		try {
			return new LockingIteration<Namespace, SailException>(queryLock,
					new IteratorIteration<Namespace, SailException>(store.getNamespaceStore().iterator()));
		}
		catch (RuntimeException e) {
			queryLock.release();
			throw e;
		}
	}

	@Override
	protected String getNamespaceInternal(String prefix)
		throws SailException
	{
		Lock queryLock = store.getQueryReadLock();
		try {
			return store.getNamespaceStore().getNamespace(prefix);
		}
		finally {
			queryLock.release();
		}
	}

	@Override
	protected void startTransactionInternal()
		throws SailException
	{
		if (!store.isWritable()) {
			throw new SailException("Unable to start transaction: data file is read-only");
		}

		// Prevent concurrent transactions by acquiring an exclusive txn lock
		txnLock = store.getTransactionLock();
		store.stopSyncTimer();
	}

	@Override
	protected void commitInternal()
		throws SailException
	{
		// Prevent querying during commit:
		Lock queryLock = store.getQueryWriteLock();

		try {
			store.commit();
		}
		finally {
			queryLock.release();
		}

		txnLock.release();
		store.startSyncTimer();
	}

	@Override
	protected void rollbackInternal()
		throws SailException
	{
		try {
			store.rollback();
		}
		finally {
			txnLock.release();
		}
	}

	@Override
	protected void addStatementInternal(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		_addStatement(subj, pred, obj, true, contexts);
	}

	public boolean addInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();

			Lock txnLock = getTransactionLock();
			try {
				autoStartTransaction();
				return _addStatement(subj, pred, obj, false, contexts);
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	/**
	 * Adds the specified statement to this MemoryStore.
	 * 
	 * @throws SailException
	 */
	private boolean _addStatement(Resource subj, URI pred, Value obj, boolean explicit, Resource... contexts)
		throws SailException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		Statement st = null;
		if (contexts.length == 0) {
			st = store.addStatement(subj, pred, obj, null, explicit);
			if (st != null) {
				notifyStatementAdded(st);
			}
		}
		else {
			for (Resource context : contexts) {
				st = store.addStatement(subj, pred, obj, context, explicit);
				if (st != null) {
					notifyStatementAdded(st);
				}
			}
		}
		return st != null;
	}

	@Override
	protected void removeStatementsInternal(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		_removeStatements(subj, pred, obj, true, contexts);
	}

	public boolean removeInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();

			Lock txnLock = getTransactionLock();
			try {
				autoStartTransaction();
				return _removeStatements(subj, pred, obj, false, contexts);
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
	protected void clearInternal(Resource... contexts)
		throws SailException
	{
		_removeStatements(null, null, null, true, contexts);
	}

	public void clearInferredFromContext(Resource... contexts)
		throws SailException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();

			Lock txnLock = getTransactionLock();
			try {
				autoStartTransaction();
				_removeStatements(null, null, null, false, contexts);
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	public void clearInferred()
		throws SailException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			verifyIsOpen();

			Lock txnLock = getTransactionLock();
			try {
				autoStartTransaction();
				_removeStatements(null, null, null, false);
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
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
		CloseableIteration<MemStatement, SailException> stIter = store.createStatementIterator(
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
					notifyStatementRemoved(st);
					statementsRemoved = true;
				}
				else if (txnStatus == TxnStatus.NEW && st.isExplicit() == explicit) {
					st.setTxnStatus(TxnStatus.ZOMBIE);
					notifyStatementRemoved(st);
					statementsRemoved = true;
				}
				else if (txnStatus == TxnStatus.EXPLICIT && !st.isExplicit() && explicit) {
					st.setTxnStatus(TxnStatus.NEUTRAL);
				}
				else if (txnStatus == TxnStatus.INFERRED && st.isExplicit() && !explicit) {
					st.setTxnStatus(TxnStatus.DEPRECATED);
					notifyStatementRemoved(st);
					statementsRemoved = true;
				}
			}
		}
		finally {
			stIter.close();
		}

		return statementsRemoved;

	}

	@Override
	protected void setNamespaceInternal(String prefix, String name)
		throws SailException
	{
		// FIXME: changes to namespace prefixes not isolated in transactions yet
		try {
			store.getNamespaceStore().setNamespace(prefix, name);
		}
		catch (IllegalArgumentException e) {
			throw new SailException(e.getMessage());
		}
	}

	@Override
	protected void removeNamespaceInternal(String prefix)
		throws SailException
	{
		store.getNamespaceStore().removeNamespace(prefix);
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
			return store.createStatementIterator(QueryEvaluationException.class, subj, pred, obj,
					!_includeInferred, ReadMode.COMMITTED, contexts);
		}

		public MemValueFactory getValueFactory() {
			return store.getValueFactory();
		}
	} // end inner class MemTripleSource

}
