/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */

package org.openrdf.sail.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.aduna.concurrent.locks.Lock;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;
import info.aduna.iteration.LockingIteration;

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
import org.openrdf.query.algebra.evaluation.impl.CompareOptimizer;
import org.openrdf.query.algebra.evaluation.impl.ConjunctiveConstraintSplitter;
import org.openrdf.query.algebra.evaluation.impl.ConstantOptimizer;
import org.openrdf.query.algebra.evaluation.impl.DisjunctiveConstraintOptimizer;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStatistics;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.algebra.evaluation.impl.FilterOptimizer;
import org.openrdf.query.algebra.evaluation.impl.IterativeEvaluationOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryJoinOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryModelNormalizer;
import org.openrdf.query.algebra.evaluation.impl.SameTermFilterOptimizer;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailReadOnlyException;
import org.openrdf.sail.helpers.NotifyingSailConnectionBase;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.memory.model.MemResource;
import org.openrdf.sail.memory.model.MemStatement;
import org.openrdf.sail.memory.model.MemStatementIterator;
import org.openrdf.sail.memory.model.MemStatementList;
import org.openrdf.sail.memory.model.MemURI;
import org.openrdf.sail.memory.model.MemValue;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.openrdf.sail.memory.model.ReadMode;

/**
 * Implementation of a Sail Connection for memory stores.
 * 
 * @author Arjohn Kampman
 * @author jeen
 */
public class MemoryStoreConnection extends NotifyingSailConnectionBase implements InferencerConnection {

	/*-----------*
	 * Variables *
	 *-----------*/

	protected final MemoryStore store;

	/**
	 * The exclusive transaction lock held by this connection during
	 * transactions.
	 */
	private volatile Lock txnLock;

	/**
	 * A statement list read lock held by this connection during transactions.
	 * Keeping this lock prevents statements from being removed from the main
	 * statement list during transactions.
	 */
	private volatile Lock txnStLock;

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected MemoryStoreConnection(MemoryStore store) {
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
		logger.trace("Incoming query model:\n{}", tupleExpr.toString());

		// Clone the tuple expression to allow for more aggresive optimizations
		tupleExpr = tupleExpr.clone();

		if (!(tupleExpr instanceof QueryRoot)) {
			// Add a dummy root node to the tuple expressions to allow the
			// optimizers to modify the actual root node
			tupleExpr = new QueryRoot(tupleExpr);
		}

		Lock stLock = store.getStatementsReadLock();

		try {
			int snapshot = store.getCurrentSnapshot();
			ReadMode readMode = ReadMode.COMMITTED;

			if (transactionActive()) {
				snapshot++;
				readMode = ReadMode.TRANSACTION;
			}

			TripleSource tripleSource = new MemTripleSource(includeInferred, snapshot, readMode);
			EvaluationStrategyImpl strategy = new EvaluationStrategyImpl(tripleSource, dataset);

			new BindingAssigner().optimize(tupleExpr, dataset, bindings);
			new ConstantOptimizer(strategy).optimize(tupleExpr, dataset, bindings);
			new CompareOptimizer().optimize(tupleExpr, dataset, bindings);
			new ConjunctiveConstraintSplitter().optimize(tupleExpr, dataset, bindings);
			new DisjunctiveConstraintOptimizer().optimize(tupleExpr, dataset, bindings);
			new SameTermFilterOptimizer().optimize(tupleExpr, dataset, bindings);
			new QueryModelNormalizer().optimize(tupleExpr, dataset, bindings);
			new QueryJoinOptimizer(new MemEvaluationStatistics()).optimize(tupleExpr, dataset, bindings);
			new IterativeEvaluationOptimizer().optimize(tupleExpr, dataset, bindings);
			new FilterOptimizer().optimize(tupleExpr, dataset, bindings);

			logger.trace("Optimized query model:\n{}", tupleExpr.toString());

			CloseableIteration<BindingSet, QueryEvaluationException> iter;
			iter = strategy.evaluate(tupleExpr, EmptyBindingSet.getInstance());
			return new LockingIteration<BindingSet, QueryEvaluationException>(stLock, iter);
		}
		catch (QueryEvaluationException e) {
			stLock.release();
			throw new SailException(e);
		}
		catch (RuntimeException e) {
			stLock.release();
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
		// Note: we can't do this in a streaming fashion due to concurrency
		// issues; iterating over the set of URIs or bnodes while another thread
		// adds statements with new resources would result in
		// ConcurrentModificationException's (issue SES-544).

		// Create a list of all resources that are used as contexts
		ArrayList<MemResource> contextIDs = new ArrayList<MemResource>(32);

		Lock stLock = store.getStatementsReadLock();

		try {
			final int snapshot = transactionActive() ? store.getCurrentSnapshot() + 1
					: store.getCurrentSnapshot();
			final ReadMode readMode = transactionActive() ? ReadMode.TRANSACTION : ReadMode.COMMITTED;

			MemValueFactory valueFactory = store.getValueFactory();

			synchronized (valueFactory) {
				for (MemResource memResource : valueFactory.getMemURIs()) {
					if (isContextResource(memResource, snapshot, readMode)) {
						contextIDs.add(memResource);
					}
				}

				for (MemResource memResource : valueFactory.getMemBNodes()) {
					if (isContextResource(memResource, snapshot, readMode)) {
						contextIDs.add(memResource);
					}
				}
			}
		}
		finally {
			stLock.release();
		}

		return new CloseableIteratorIteration<MemResource, SailException>(contextIDs.iterator());
	}

	private boolean isContextResource(MemResource memResource, int snapshot, ReadMode readMode)
		throws SailException
	{
		MemStatementList contextStatements = memResource.getContextStatementList();

		// Filter resources that are not used as context identifier
		if (contextStatements.size() == 0) {
			return false;
		}

		// Filter more thoroughly by considering snapshot and read-mode parameters
		MemStatementIterator<SailException> iter = new MemStatementIterator<SailException>(contextStatements,
				null, null, null, false, snapshot, readMode);
		try {
			return iter.hasNext();
		}
		finally {
			iter.close();
		}
	}

	@Override
	protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(Resource subj,
			URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws SailException
	{
		Lock stLock = store.getStatementsReadLock();

		try {
			int snapshot = store.getCurrentSnapshot();
			ReadMode readMode = ReadMode.COMMITTED;

			if (transactionActive()) {
				snapshot++;
				readMode = ReadMode.TRANSACTION;
			}

			return new LockingIteration<MemStatement, SailException>(stLock, store.createStatementIterator(
					SailException.class, subj, pred, obj, !includeInferred, snapshot, readMode, contexts));
		}
		catch (RuntimeException e) {
			stLock.release();
			throw e;
		}
	}

	public boolean hasStatement(Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts)
		throws SailException
	{
		Lock stLock = store.getStatementsReadLock();

		try {
			int snapshot = store.getCurrentSnapshot();
			ReadMode readMode = ReadMode.COMMITTED;

			if (transactionActive()) {
				snapshot++;
				readMode = ReadMode.TRANSACTION;
			}

			return store.hasStatement(subj, pred, obj, !includeInferred, snapshot, readMode, contexts);
		}
		finally {
			stLock.release();
		}
	}

	@Override
	protected long sizeInternal(Resource... contexts)
		throws SailException
	{
		Lock stLock = store.getStatementsReadLock();

		try {
			CloseableIteration<? extends Statement, SailException> iter = getStatementsInternal(null, null,
					null, false, contexts);

			try {
				long size = 0L;

				while (iter.hasNext()) {
					iter.next();
					size++;
				}

				return size;
			}
			finally {
				iter.close();
			}
		}
		finally {
			stLock.release();
		}
	}

	@Override
	protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal()
		throws SailException
	{
		return new CloseableIteratorIteration<Namespace, SailException>(store.getNamespaceStore().iterator());
	}

	@Override
	protected String getNamespaceInternal(String prefix)
		throws SailException
	{
		return store.getNamespaceStore().getNamespace(prefix);
	}

	@Override
	protected void startTransactionInternal()
		throws SailException
	{
		if (!store.isWritable()) {
			throw new SailReadOnlyException("Unable to start transaction: data file is locked or read-only");
		}

		txnStLock = store.getStatementsReadLock();

		try {
			// Prevent concurrent transactions by acquiring an exclusive txn lock
			txnLock = store.getTransactionLock();

			try {
				store.startTransaction();
			}
			catch (SailException e) {
				txnLock.release();
				throw e;
			}
			catch (RuntimeException e) {
				txnLock.release();
				throw e;
			}
		}
		catch (SailException e) {
			txnStLock.release();
			throw e;
		}
		catch (RuntimeException e) {
			txnStLock.release();
			throw e;
		}
	}

	@Override
	protected void commitInternal()
		throws SailException
	{
		store.commit();
		txnLock.release();
		txnStLock.release();
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
			txnStLock.release();
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
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();

			updateLock.lock();
			try {
				autoStartTransaction();
				return addStatementInternal(subj, pred, obj, false, contexts);
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
	 * Adds the specified statement to this MemoryStore.
	 * 
	 * @throws SailException
	 */
	protected boolean addStatementInternal(Resource subj, URI pred, Value obj, boolean explicit,
			Resource... contexts)
		throws SailException
	{
		assert txnStLock.isActive();
		assert txnLock.isActive();

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

		// FIXME: this return type is invalid in case multiple contexts were
		// specified
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
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();

			updateLock.lock();
			try {
				autoStartTransaction();
				return removeStatementsInternal(subj, pred, obj, false, contexts);
			}
			finally {
				updateLock.unlock();
			}
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	@Override
	protected void clearInternal(Resource... contexts)
		throws SailException
	{
		removeStatementsInternal(null, null, null, true, contexts);
	}

	public void clearInferred(Resource... contexts)
		throws SailException
	{
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();

			updateLock.lock();
			try {
				autoStartTransaction();
				removeStatementsInternal(null, null, null, false, contexts);
			}
			finally {
				updateLock.unlock();
			}
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	public void flushUpdates() {
		// no-op; changes are reported as soon as they come in
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
	 *        pattern, <tt>false</tt> removes inferred statements that match the
	 *        pattern.
	 * @throws SailException
	 */
	protected boolean removeStatementsInternal(Resource subj, URI pred, Value obj, boolean explicit,
			Resource... contexts)
		throws SailException
	{
		CloseableIteration<MemStatement, SailException> stIter = store.createStatementIterator(
				SailException.class, subj, pred, obj, explicit, store.getCurrentSnapshot() + 1,
				ReadMode.TRANSACTION, contexts);

		return removeIteratorStatements(stIter, explicit);
	}

	protected boolean removeIteratorStatements(CloseableIteration<MemStatement, SailException> stIter,
			boolean explicit)
		throws SailException
	{
		boolean statementsRemoved = false;

		try {
			while (stIter.hasNext()) {
				MemStatement st = stIter.next();

				if (store.removeStatement(st, explicit)) {
					statementsRemoved = true;
					notifyStatementRemoved(st);
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

		protected final int snapshot;

		protected final ReadMode readMode;

		protected final boolean includeInferred;

		public MemTripleSource(boolean includeInferred, int snapshot, ReadMode readMode) {
			this.includeInferred = includeInferred;
			this.snapshot = snapshot;
			this.readMode = readMode;
		}

		public CloseableIteration<MemStatement, QueryEvaluationException> getStatements(Resource subj,
				URI pred, Value obj, Resource... contexts)
		{
			return store.createStatementIterator(QueryEvaluationException.class, subj, pred, obj,
					!includeInferred, snapshot, readMode, contexts);
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
		protected CardinalityCalculator createCardinalityCalculator() {
			return new MemCardinalityCalculator();
		}

		protected class MemCardinalityCalculator extends CardinalityCalculator {

			@Override
			public double getCardinality(StatementPattern sp) {
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
					return 0.0;
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

				double cardinality;

				if (listSizes.isEmpty()) {
					// all wildcards
					cardinality = store.size();
				}
				else {
					cardinality = Collections.min(listSizes);

					// List<Var> vars = getVariables(sp);
					// int constantVarCount = countConstantVars(vars);
					//
					// // Subtract 1 from var count as this was used for the list
					// size
					// double unboundVarFactor = (double)(vars.size() -
					// constantVarCount) / (vars.size() - 1);
					//
					// cardinality = Math.pow(cardinality, unboundVarFactor);
				}

				return cardinality;
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
