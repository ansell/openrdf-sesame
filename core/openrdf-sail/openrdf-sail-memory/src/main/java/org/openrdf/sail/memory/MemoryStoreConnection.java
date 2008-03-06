/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.querylogic.EvaluationStrategy;
import org.openrdf.querylogic.QuerySolution;
import org.openrdf.querylogic.TripleSource;
import org.openrdf.querylogic.impl.EvaluationStrategyImpl;
import org.openrdf.querymodel.GraphQuery;
import org.openrdf.querymodel.QueryOptimizer;
import org.openrdf.querymodel.TupleExpr;
import org.openrdf.querymodel.TupleQuery;
import org.openrdf.queryresult.GraphQueryResult;
import org.openrdf.queryresult.Solution;
import org.openrdf.queryresult.TupleQueryResult;
import org.openrdf.sail.InferencerConnection;
import org.openrdf.sail.Namespace;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailInternalException;
import org.openrdf.sail.helpers.SailConnectionBase;
import org.openrdf.sail.helpers.SailGraphQueryResult;
import org.openrdf.sail.helpers.SailTupleQueryResult;
import org.openrdf.sail.memory.model.MemResource;
import org.openrdf.sail.memory.model.MemStatement;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.openrdf.sail.memory.model.ReadMode;
import org.openrdf.sail.memory.model.TxnStatus;
import org.openrdf.util.iterator.CloseableIterator;
import org.openrdf.util.iterator.ConvertingIterator;
import org.openrdf.util.iterator.FilterIterator;
import org.openrdf.util.iterator.LockingIterator;
import org.openrdf.util.iterator.UnionIterator;
import org.openrdf.util.locking.Lock;

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

	protected TupleQueryResult _evaluate(TupleQuery query, boolean includeInferred) {
		return _evaluate(query.getTupleExpr(), includeInferred);
	}

	protected GraphQueryResult _evaluate(GraphQuery query, boolean includeInferred) {
		final TupleQueryResult result = _evaluate(query.getTupleExpr(), includeInferred);

		// create a FilterIterator that filters out all partial matches (this can happen
		// if the query contains an optional path expression)
		FilterIterator<Solution> filterIter = new FilterIterator<Solution>(result.iterator()) {

			protected boolean accept(Solution solution) {
				return solution.getValue("subject") instanceof Resource
						&& solution.getValue("predicate") instanceof URI
						&& solution.getValue("object") instanceof Value;
			}
		};

		// create a ConvertingIterator that converts Solution objects to actual RDF statements.
		CloseableIterator<Statement> iter = new ConvertingIterator<Solution, Statement>(filterIter) {

			protected Statement convert(Solution solution) {
				try {
					Resource subject = (Resource)solution.getValue("subject");
					URI predicate = (URI)solution.getValue("predicate");
					Value object = solution.getValue("object");
					Resource context = (Resource)solution.getValue("context");
					if (context == null) {
						return _store.getValueFactory().createStatement(subject, predicate, object);
					}
					else {
						return _store.getValueFactory().createStatement(subject, predicate, object, context);
					}
				}
				catch (ClassCastException e) {
					// TODO this should probably be a more specific exception
					throw new SailInternalException("Unexpected value type: " + e.getMessage());
				}

			}

			public void close() {
				if (!super.isClosed()) {
					result.close();
				}

				super.close();
			}
		};

		return new SailGraphQueryResult(null, iter);
	}

	private TupleQueryResult _evaluate(TupleExpr tupleExpr, boolean includeInferred) {
		// Apply the default optimizations
		tupleExpr = QueryOptimizer.optimize(tupleExpr);

		Lock queryLock = _store.getQueryReadLock();
		try {
			EvaluationStrategy strategy = new EvaluationStrategyImpl();
			CloseableIterator<Solution> iter = strategy.evaluate(tupleExpr,
					new MemTripleSource(includeInferred), new QuerySolution());
			iter = new LockingIterator<Solution>(queryLock, iter);

			return new SailTupleQueryResult(tupleExpr.getBindingNames(), iter);
		}
		catch (RuntimeException e) {
			queryLock.release();
			throw e;
		}
	}

	protected CloseableIterator<? extends Resource> _getContextIDs() {
		Lock queryLock = _store.getQueryReadLock();

		try {
			// Iterate over all MemURIs and MemBNodes
			CloseableIterator<MemResource> iter = new UnionIterator<MemResource>(
					_store.getValueFactory().getMemURIs().iterator(),
					_store.getValueFactory().getMemBNodes().iterator());

			// Only return the resources that are actually used as a context
			// identifier
			iter = new FilterIterator<MemResource>(iter) {

				protected boolean accept(MemResource memResource) {
					return memResource.getContextStatementCount() > 0;
				}
			};

			// Release query lock when iterator is closed
			iter = new LockingIterator<MemResource>(queryLock, iter);

			return iter;
		}
		catch (RuntimeException e) {
			queryLock.release();
			throw e;
		}
	}

	protected CloseableIterator<? extends Statement> _getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred)
	{
		Lock queryLock = _store.getQueryReadLock();

		try {
			return new LockingIterator<MemStatement>(queryLock, _store.createStatementIterator(subj, pred, obj,
					null, !includeInferred, false, ReadMode.COMMITTED));
		}
		catch (RuntimeException e) {
			queryLock.release();
			throw e;
		}
	}

	protected CloseableIterator<? extends Statement> _getNullContextStatements(Resource subj, URI pred,
			Value obj, boolean includeInferred)
	{
		Lock queryLock = _store.getQueryReadLock();
		try {
			return new LockingIterator<MemStatement>(queryLock, _store.createStatementIterator(subj, pred, obj,
					_store.getValueFactory().getNullContext(), !includeInferred, false, ReadMode.COMMITTED));
		}
		catch (RuntimeException e) {
			queryLock.release();
			throw e;
		}
	}

	protected CloseableIterator<? extends Statement> _getNamedContextStatements(Resource subj, URI pred,
			Value obj, Resource context, boolean includeInferred)
	{
		Lock queryLock = _store.getQueryReadLock();
		try {
			return new LockingIterator<MemStatement>(queryLock, _store.createStatementIterator(subj, pred, obj,
					context, !includeInferred, true, ReadMode.COMMITTED));
		}
		catch (RuntimeException e) {
			queryLock.release();
			throw e;
		}
	}

	protected CloseableIterator<? extends Namespace> _getNamespaces() {
		Lock queryLock = _store.getQueryReadLock();
		try {
			return new LockingIterator<Namespace>(queryLock, _store.getValueFactory().getNamespaces().iterator());
		}
		catch (RuntimeException e) {
			queryLock.release();
			throw e;
		}
	}

	protected void _startTransaction()
		throws SailException
	{
		if (!_store.isWritable()) {
			throw new SailException("Unable to start transaction: data file is read-only");
		}

		try {
			// Prevent concurrent transactions by acquiring an exclusive txn lock
			_txnLock = _store.getTransactionLock();
			_store.stopSyncTimer();
		}
		catch (InterruptedException e) {
			throw new SailException("Failed to acquire transaction lock", e);
		}
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

	protected void _addStatement(Resource subj, URI pred, Value obj, Resource context)
		throws SailException
	{
		if (context == null) {
			context = _store.getValueFactory().getNullContext();
		}

		_addStatement(subj, pred, obj, context, true);
	}

	public boolean addInferredStatement(Resource subj, URI pred, Value obj, Resource context)
		throws SailException
	{
		_autoStartTransaction();

		if (context == null) {
			context = _store.getValueFactory().getNullContext();
		}

		return _addStatement(subj, pred, obj, context, false);
	}

	/**
	 * Adds the specified statement to this MemoryStore.
	 */
	private boolean _addStatement(Resource subj, URI pred, Value obj, Resource context, boolean explicit) {
		Statement st = _store.addStatement(subj, pred, obj, context, explicit);

		if (st != null) {
			_notifyStatementAdded(st);
		}

		return st != null;
	}

	protected void _removeStatement(Resource subj, URI pred, Value obj, Resource context)
		throws SailException
	{
		if (context == null) {
			context = _store.getValueFactory().getNullContext();
		}

		_removeStatements(subj, pred, obj, context, true);
	}

	public boolean removeInferredStatement(Resource subj, URI pred, Value obj, Resource context)
		throws SailException
	{
		_autoStartTransaction();

		if (context == null) {
			context = _store.getValueFactory().getNullContext();
		}

		return _removeStatements(subj, pred, obj, context, false);
	}

	protected void _clearContext(Resource context)
		throws SailException
	{
		if (context == null) {
			context = _store.getValueFactory().getNullContext();
		}

		_removeStatements(null, null, null, context, true);
	}

	protected void _clear()
		throws SailException
	{
		_removeStatements(null, null, null, null, true);
	}

	public void clearInferredFromContext(Resource context)
		throws SailException
	{
		_autoStartTransaction();

		if (context == null) {
			context = _store.getValueFactory().getNullContext();
		}

		_removeStatements(null, null, null, context, false);
	}

	public void clearInferred()
		throws SailException
	{
		_autoStartTransaction();

		_removeStatements(null, null, null, null, false);
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
	 * @param context
	 *        The context for the pattern, or <tt>null</tt> for a wildcard.
	 * @param explicit
	 *        Flag indicating whether explicit or inferred statements should be
	 *        removed; <tt>true</tt> removes explicit statements that match the
	 *        pattern, <tt>false</tt> removes inferred statements that match
	 *        the pattern.
	 */
	private boolean _removeStatements(Resource subj, URI pred, Value obj, Resource context, boolean explicit) {
		CloseableIterator<MemStatement> stIter = _store.createStatementIterator(subj, pred, obj, context,
				false, false, ReadMode.TRANSACTION);

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
		// FIXME: changes to namespace prefixes not isolated yet
		try {
			_store.getValueFactory().setNamespace(prefix, name);
		}
		catch (IllegalArgumentException e) {
			throw new SailException(e.getMessage());
		}
	}

	protected void _removeNamespace(String prefix)
		throws SailException
	{
		_store.getValueFactory().removeNamespace(prefix);
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

		public CloseableIterator<MemStatement> getStatements(Resource subj, URI pred, Value obj) {
			return _store.createStatementIterator(subj, pred, obj, null, !_includeInferred, false,
					ReadMode.COMMITTED);
		}

		public CloseableIterator<MemStatement> getNullContextStatements(Resource subj, URI pred, Value obj) {
			return _store.createStatementIterator(subj, pred, obj, getValueFactory().getNullContext(),
					!_includeInferred, false, ReadMode.COMMITTED);
		}

		public CloseableIterator<MemStatement> getNamedContextStatements(Resource subj, URI pred, Value obj,
				Resource context)
		{
			return _store.createStatementIterator(subj, pred, obj, context, !_includeInferred, true,
					ReadMode.COMMITTED);
		}

		public MemValueFactory getValueFactory() {
			return _store.getValueFactory();
		}
	} // end inner class MemTripleSource
}
