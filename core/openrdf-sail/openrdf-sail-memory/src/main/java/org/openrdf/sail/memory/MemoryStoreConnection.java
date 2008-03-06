/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */

package org.openrdf.sail.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.impl.BindingAssigner;
import org.openrdf.query.algebra.evaluation.impl.ConjunctiveConstraintSplitter;
import org.openrdf.query.algebra.evaluation.impl.ConstantOptimizer;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStatistics;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.algebra.evaluation.impl.FilterOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryJoinOptimizer;
import org.openrdf.query.algebra.evaluation.util.QueryOptimizerList;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionBase;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.memory.model.MemResource;
import org.openrdf.sail.memory.model.MemStatement;
import org.openrdf.sail.memory.model.MemURI;
import org.openrdf.sail.memory.model.MemValue;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.openrdf.sail.memory.model.ReadMode;
import org.openrdf.sail.memory.model.TxnStatus;

/**
 * Implementation of a Sail Connection for memory stores.
 * 
 * @author Arjohn Kampman
 * @author jeen
 */
public class MemoryStoreConnection extends SailConnectionBase implements InferencerConnection {

	/*-----------*
	 * Variables *
	 *-----------*/

	protected final MemoryStore store;

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
			TupleExpr tupleExpr, Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		// FIXME: handle datasets properly
//		if (dataset != null) {
//			throw new SailException("Datasets not supported");
//		}
		
		// Clone the tuple expression to allow for more aggresive optimizations
		tupleExpr = tupleExpr.clone();

		if (!(tupleExpr instanceof QueryRoot)) {
			tupleExpr = new QueryRoot(tupleExpr);
		}

		Lock queryLock = store.getQueryReadLock();

		try {
			TripleSource tripleSource = new MemTripleSource(includeInferred);
			EvaluationStrategyImpl strategy = new EvaluationStrategyImpl(tripleSource, dataset);

			QueryOptimizerList optimizerList = new QueryOptimizerList();
			optimizerList.add(new BindingAssigner());
			optimizerList.add(new ConstantOptimizer(strategy));
			// replace compares with sameterms
			optimizerList.add(new QueryJoinOptimizer(new MemEvaluationStatistics()));
			optimizerList.add(new ConjunctiveConstraintSplitter());
			optimizerList.add(new FilterOptimizer());

			optimizerList.optimize(tupleExpr, dataset, bindings);

			CloseableIteration<BindingSet, QueryEvaluationException> iter;
			iter = strategy.evaluate(tupleExpr, bindings);
			return new LockingIteration<BindingSet, QueryEvaluationException>(queryLock, iter);
		}
		catch (QueryEvaluationException e) {
			queryLock.release();
			throw new SailException(e);
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
		addStatementInternal(subj, pred, obj, true, contexts);
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
				return addStatementInternal(subj, pred, obj, false, contexts);
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
	protected boolean addStatementInternal(Resource subj, URI pred, Value obj, boolean explicit,
			Resource... contexts)
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
		removeStatementsInternal(subj, pred, obj, true, contexts);
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
				return removeStatementsInternal(subj, pred, obj, false, contexts);
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
		removeStatementsInternal(null, null, null, true, contexts);
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
				removeStatementsInternal(null, null, null, false, contexts);
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
				removeStatementsInternal(null, null, null, false);
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
	protected boolean removeStatementsInternal(Resource subj, URI pred, Value obj, boolean explicit,
			Resource... contexts)
		throws SailException
	{
		CloseableIteration<MemStatement, SailException> stIter = store.createStatementIterator(
				SailException.class, subj, pred, obj, explicit, ReadMode.TRANSACTION, contexts);

		return removeIteratorStatements(stIter, explicit);
	}

	protected <X extends Exception> boolean removeIteratorStatements(
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
		// FIXME: changes to namespace prefixes not isolated in transactions yet
		store.getNamespaceStore().removeNamespace(prefix);
	}

	@Override
	protected void clearNamespacesInternal()
		throws SailException
	{
		// FIXME: changes to namespace prefixes not isolated in transactions yet
		store.getNamespaceStore().clear();
	}

	/*-----------------------------*
	 * Inner class MemTripleSource *
	 *-----------------------------*/

	/**
	 * Implementation of the TripleSource interface from the Sail Query Model
	 */
	protected class MemTripleSource implements TripleSource {

		protected final boolean includeInferred;

		public MemTripleSource(boolean includeInferred) {
			this.includeInferred = includeInferred;
		}

		public CloseableIteration<MemStatement, QueryEvaluationException> getStatements(Resource subj,
				URI pred, Value obj, Resource... contexts)
		{
			return store.createStatementIterator(QueryEvaluationException.class, subj, pred, obj,
					!includeInferred, ReadMode.COMMITTED, contexts);
		}

		public MemValueFactory getValueFactory() {
			return store.getValueFactory();
		}
	} // end inner class MemTripleSource

	/*-------------------------------------*
	 * Inner class MemEvaluationStatistics *
	 *-------------------------------------*/

	/**
	 * Uses the MemoryStore's statement sizes to give cost estimates based on the
	 * size of the expected results. This process could be improved with
	 * repository statistics about size and distribution of statements.
	 * 
	 * @author Arjohn Kampman
	 * @author James Leigh
	 */
	protected class MemEvaluationStatistics extends EvaluationStatistics {

		@Override
		protected CardinalityCalculator getCardinalityCalculator(Set<String> boundVars)
		{
			return new MemCardinalityCalculator(boundVars);
		}

		protected class MemCardinalityCalculator extends CardinalityCalculator {

			public MemCardinalityCalculator(Set<String> boundVars) {
				super(boundVars);
			}

			@Override
			public void meet(StatementPattern sp)
			{
				Resource subj = (Resource)getConstantValue(sp.getSubjectVar());
				URI pred = (URI)getConstantValue(sp.getPredicateVar());
				Value obj = getConstantValue(sp.getObjectVar());
				Resource context = (Resource)getConstantValue(sp.getContextVar());

				MemValueFactory valueFactory = store.getValueFactory();

				// Perform look-ups for value-equivalents of the specified values
				MemResource memSubj = valueFactory.getMemResource(subj);
				MemURI memPred = valueFactory.getMemURI(pred);
				MemValue memObj = valueFactory.getMemValue(obj);
				MemResource memContext = valueFactory.getMemResource(context);

				if (subj != null && memSubj == null || pred != null && memPred == null || obj != null
						&& memObj == null || context != null && memContext == null)
				{
					// non-existent subject, predicate, object or context
					cardinality = 0;
					return;
				}

				// Search for the smallest list that can be used by the iterator
				List<Integer> listSizes = new ArrayList<Integer>(4);
				if (memSubj != null) {
					listSizes.add(memSubj.getSubjectStatementCount());
				}
				if (memPred != null) {
					listSizes.add(memPred.getPredicateStatementCount());
				}
				if (memObj != null) {
					listSizes.add(memObj.getObjectStatementCount());
				}
				if (memContext != null) {
					listSizes.add(memContext.getContextStatementCount());
				}

				if (listSizes.isEmpty()) {
					cardinality = store.size();

					int sqrtFactor = 2 * countBoundVars(sp);

					if (sqrtFactor > 1) {
						cardinality = Math.pow(cardinality, 1.0 / sqrtFactor);
					}
				}
				else {
					cardinality = Collections.min(listSizes);

					int constantVarCount = countConstantVars(sp);
					int boundVarCount = countBoundVars(sp);

					// Subtract 1 from constantVarCount as this was used for the list
					// size
					int sqrtFactor = 2 * boundVarCount + Math.max(0, constantVarCount - 1);

					if (sqrtFactor > 1) {
						cardinality = Math.pow(cardinality, 1.0 / sqrtFactor);
					}
				}
			}

			protected Value getConstantValue(Var var) {
				if (var != null) {
					return var.getValue();
				}

				return null;
			}
		}
	} // end inner class MemCardinalityCalculator
}
