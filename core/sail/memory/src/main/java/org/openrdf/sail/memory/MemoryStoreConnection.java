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

import info.aduna.concurrent.locks.Lock;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.cursors.LockingCursor;
import org.openrdf.query.algebra.evaluation.impl.BindingAssigner;
import org.openrdf.query.algebra.evaluation.impl.CompareOptimizer;
import org.openrdf.query.algebra.evaluation.impl.ConjunctiveConstraintSplitter;
import org.openrdf.query.algebra.evaluation.impl.ConstantOptimizer;
import org.openrdf.query.algebra.evaluation.impl.DisjunctiveConstraintOptimizer;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStatistics;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.algebra.evaluation.impl.FilterOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryJoinOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryModelPruner;
import org.openrdf.query.algebra.evaluation.impl.SameTermFilterOptimizer;
import org.openrdf.query.algebra.evaluation.util.QueryOptimizerList;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.results.Cursor;
import org.openrdf.results.impl.IteratorCursor;
import org.openrdf.sail.SailReadOnlyException;
import org.openrdf.sail.helpers.NotifyingSailConnectionBase;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.memory.model.MemResource;
import org.openrdf.sail.memory.model.MemStatement;
import org.openrdf.sail.memory.model.MemStatementCursor;
import org.openrdf.sail.memory.model.MemStatementList;
import org.openrdf.sail.memory.model.MemURI;
import org.openrdf.sail.memory.model.MemValue;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.openrdf.sail.memory.model.ReadMode;
import org.openrdf.store.StoreException;

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
	private Lock txnLock;

	/**
	 * A statement list read lock held by this connection during transactions.
	 * Keeping this lock prevents statements from being removed from the main
	 * statement list during transactions.
	 */
	private Lock txnStLock;

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected MemoryStoreConnection(MemoryStore store) {
		this.store = store;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Cursor<? extends BindingSet> evaluate(QueryModel query, BindingSet bindings,
			boolean includeInferred)
		throws StoreException
	{
		logger.trace("Incoming query model:\n{}", query.toString());

		// Clone the tuple expression to allow for more aggresive optimizations
		query = query.clone();

		Lock stLock = store.getStatementsReadLock();

		try {
			int snapshot = store.getCurrentSnapshot();
			ReadMode readMode = ReadMode.COMMITTED;

			if (transactionActive()) {
				snapshot++;
				readMode = ReadMode.TRANSACTION;
			}

			TripleSource tripleSource = new MemTripleSource(includeInferred, snapshot, readMode);
			EvaluationStrategyImpl strategy = new EvaluationStrategyImpl(tripleSource, query);

			QueryOptimizerList optimizerList = new QueryOptimizerList();
			optimizerList.add(new BindingAssigner());
			optimizerList.add(new ConstantOptimizer(strategy));
			optimizerList.add(new CompareOptimizer());
			optimizerList.add(new ConjunctiveConstraintSplitter());
			optimizerList.add(new DisjunctiveConstraintOptimizer());
			optimizerList.add(new SameTermFilterOptimizer());
			optimizerList.add(new QueryModelPruner());
			optimizerList.add(new QueryJoinOptimizer(new MemEvaluationStatistics()));
			optimizerList.add(new FilterOptimizer());

			optimizerList.optimize(query, bindings);

			logger.trace("Optimized query model:\n{}", query.toString());

			Cursor<BindingSet> iter;
			iter = strategy.evaluate(query, EmptyBindingSet.getInstance());
			iter = new LockingCursor<BindingSet>(stLock, iter);
			return iter;
		}
		catch (StoreException e) {
			stLock.release();
			throw e;
		}
		catch (RuntimeException e) {
			stLock.release();
			throw e;
		}
	}

	public Cursor<? extends Resource> getContextIDs()
		throws StoreException
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

		return new IteratorCursor<MemResource>(contextIDs.iterator());
	}

	private boolean isContextResource(MemResource memResource, int snapshot, ReadMode readMode)
		throws StoreException
	{
		MemStatementList contextStatements = memResource.getContextStatementList();

		// Filter resources that are not used as context identifier
		if (contextStatements.size() == 0) {
			return false;
		}

		// Filter more thoroughly by considering snapshot and read-mode parameters
		MemStatementCursor iter = new MemStatementCursor(contextStatements, null, null, null, false, snapshot,
				readMode);
		try {
			return iter.next() != null;
		}
		finally {
			iter.close();
		}
	}

	public Cursor<? extends Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		Lock stLock = store.getStatementsReadLock();

		try {
			int snapshot = store.getCurrentSnapshot();
			ReadMode readMode = ReadMode.COMMITTED;

			if (transactionActive()) {
				snapshot++;
				readMode = ReadMode.TRANSACTION;
			}

			return new LockingCursor<MemStatement>(stLock, store.createStatementIterator(subj, pred, obj,
					!includeInferred, snapshot, readMode, contexts));
		}
		catch (RuntimeException e) {
			stLock.release();
			throw e;
		}
	}

	public long size(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		Lock stLock = store.getStatementsReadLock();

		try {
			Cursor<? extends Statement> iter = getStatements(subj, pred, obj, includeInferred, contexts);

			try {
				long size = 0L;

				while (iter.next() != null) {
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

	public Cursor<? extends Namespace> getNamespaces()
		throws StoreException
	{
		return new IteratorCursor<Namespace>(store.getNamespaceStore().iterator());
	}

	public String getNamespace(String prefix)
		throws StoreException
	{
		return store.getNamespaceStore().getNamespace(prefix);
	}

	@Override
	public void begin()
		throws StoreException
	{
		if (!store.isWritable()) {
			throw new SailReadOnlyException("Unable to start transaction: data file is locked or read-only");
		}

		txnStLock = store.getStatementsReadLock();

		// Prevent concurrent transactions by acquiring an exclusive txn lock
		txnLock = store.getTransactionLock();
		store.startTransaction();
		super.begin();
	}

	@Override
	public void commit()
		throws StoreException
	{
		store.commit();
		txnLock.release();
		txnStLock.release();
		super.commit();
	}

	@Override
	public void rollback()
		throws StoreException
	{
		try {
			store.rollback();
			super.rollback();
		}
		finally {
			txnLock.release();
			txnStLock.release();
		}
	}

	public void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		addStatementInternal(subj, pred, obj, true, contexts);
	}

	public boolean addInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		return addStatementInternal(subj, pred, obj, false, contexts);
	}

	/**
	 * Adds the specified statement to this MemoryStore.
	 * 
	 * @throws StoreException
	 */
	private boolean addStatementInternal(Resource subj, URI pred, Value obj, boolean explicit,
			Resource... contexts)
		throws StoreException
	{
		Statement st = null;

		if (contexts != null && contexts.length == 0) {
			st = store.addStatement(subj, pred, obj, null, explicit);
			if (st != null) {
				notifyStatementAdded(st);
			}
		}
		else {
			for (Resource context : OpenRDFUtil.notNull(contexts)) {
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

	public void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		removeStatementsInternal(subj, pred, obj, true, contexts);
	}

	public boolean removeInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		return removeStatementsInternal(subj, pred, obj, false, contexts);
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
	 * @throws StoreException
	 */
	private boolean removeStatementsInternal(Resource subj, URI pred, Value obj, boolean explicit,
			Resource... contexts)
		throws StoreException
	{
		Cursor<MemStatement> stIter = store.createStatementIterator(subj, pred, obj, explicit,
				store.getCurrentSnapshot() + 1, ReadMode.TRANSACTION, contexts);

		return removeIteratorStatements(stIter, explicit);
	}

	protected boolean removeIteratorStatements(Cursor<MemStatement> stIter, boolean explicit)
		throws StoreException
	{
		boolean statementsRemoved = false;

		try {
			MemStatement st;
			while ((st = stIter.next()) != null) {

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

	public void setNamespace(String prefix, String name)
		throws StoreException
	{
		// FIXME: changes to namespace prefixes not isolated in transactions yet
		try {
			store.getNamespaceStore().setNamespace(prefix, name);
		}
		catch (IllegalArgumentException e) {
			throw new StoreException(e.getMessage());
		}
	}

	public void removeNamespace(String prefix)
		throws StoreException
	{
		// FIXME: changes to namespace prefixes not isolated in transactions yet
		store.getNamespaceStore().removeNamespace(prefix);
	}

	public void clearNamespaces()
		throws StoreException
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

		public Cursor<MemStatement> getStatements(Resource subj, URI pred, Value obj, Resource... contexts) {
			return store.createStatementIterator(subj, pred, obj, !includeInferred, snapshot, readMode, contexts);
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
