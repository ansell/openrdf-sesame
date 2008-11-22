/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import info.aduna.concurrent.locks.Lock;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Cursor;
import org.openrdf.query.EvaluationException;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.cursors.LockingCursor;
import org.openrdf.query.algebra.evaluation.impl.BindingAssigner;
import org.openrdf.query.algebra.evaluation.impl.CompareOptimizer;
import org.openrdf.query.algebra.evaluation.impl.ConjunctiveConstraintSplitter;
import org.openrdf.query.algebra.evaluation.impl.ConstantOptimizer;
import org.openrdf.query.algebra.evaluation.impl.DisjunctiveConstraintOptimizer;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.algebra.evaluation.impl.FilterOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryJoinOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryModelPruner;
import org.openrdf.query.algebra.evaluation.impl.SameTermFilterOptimizer;
import org.openrdf.query.algebra.evaluation.util.QueryOptimizerList;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.impl.IteratorCursor;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.helpers.NotifyingSailConnectionBase;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.nativerdf.btree.RecordIterator;
import org.openrdf.sail.nativerdf.model.NativeValue;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class NativeStoreConnection extends NotifyingSailConnectionBase implements InferencerConnection {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected final NativeStore nativeStore;

	/*-----------*
	 * Variables *
	 *-----------*/

	private DefaultSailChangedEvent sailChangedEvent;

	/**
	 * The exclusive transaction lock held by this connection during
	 * transactions.
	 */
	private Lock txnLock;

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected NativeStoreConnection(NativeStore nativeStore)
		throws IOException
	{
		this.nativeStore = nativeStore;
		sailChangedEvent = new DefaultSailChangedEvent(nativeStore);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Cursor<? extends BindingSet> evaluate(QueryModel query, BindingSet bindings,
			boolean includeInferred)
		throws StoreException
	{
//		logger.trace("Incoming query model:\n{}", query.toString());

		// Clone the tuple expression to allow for more aggressive optimizations
		query = query.clone();

		Lock readLock = nativeStore.getReadLock();

		try {
			replaceValues(query);

			NativeTripleSource tripleSource = new NativeTripleSource(nativeStore, includeInferred,
					transactionActive());
			EvaluationStrategyImpl strategy = new EvaluationStrategyImpl(tripleSource, query);

			QueryOptimizerList optimizerList = new QueryOptimizerList();
			optimizerList.add(new BindingAssigner());
			optimizerList.add(new ConstantOptimizer(strategy));
			optimizerList.add(new CompareOptimizer());
			optimizerList.add(new ConjunctiveConstraintSplitter());
			optimizerList.add(new DisjunctiveConstraintOptimizer());
			optimizerList.add(new SameTermFilterOptimizer());
			optimizerList.add(new QueryModelPruner());
			optimizerList.add(new QueryJoinOptimizer(new NativeEvaluationStatistics(nativeStore)));
			optimizerList.add(new FilterOptimizer());

			optimizerList.optimize(query, bindings);
			logger.trace("Optimized query model:\n{}", query.toString());

			Cursor<BindingSet> iter;
			iter = strategy.evaluate(query, EmptyBindingSet.getInstance());
			iter = new LockingCursor<BindingSet>(readLock, iter);
			return iter;
		}
		catch (EvaluationException e) {
			readLock.release();
			throw e;
		}
		catch (RuntimeException e) {
			readLock.release();
			throw e;
		}
	}

	protected void replaceValues(TupleExpr tupleExpr)
		throws StoreException
	{
		// Replace all Value objects stored in variables with NativeValue objects,
		// which cache internal IDs
		tupleExpr.visit(new QueryModelVisitorBase<StoreException>() {

			@Override
			public void meet(Var var) {
				if (var.hasValue()) {
					var.setValue(nativeStore.getValueStore().getNativeValue(var.getValue()));
				}
			}
		});
	}

	public Cursor<? extends Resource> getContextIDs()
		throws StoreException
	{
		// Which resources are used as context identifiers is not stored
		// separately. Iterate over all statements and extract their context.
		Lock readLock = nativeStore.getReadLock();
		try {
			Cursor<? extends Resource> contextIter;
			contextIter = nativeStore.getContextIDs(transactionActive());
			// releasing the read lock when the iterator is closed
			contextIter = new LockingCursor<Resource>(readLock, contextIter);

			return contextIter;
		}
		catch (IOException e) {
			readLock.release();
			throw new StoreException(e);
		}
		catch (RuntimeException e) {
			readLock.release();
			throw e;
		}
	}

	public Cursor<? extends Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		Lock readLock = nativeStore.getReadLock();
		try {
			Cursor<? extends Statement> iter;
			iter = nativeStore.createStatementCursor(subj, pred, obj, includeInferred, transactionActive(),
					contexts);
			iter = new LockingCursor<Statement>(readLock, iter);

			return iter;
		}
		catch (IOException e) {
			readLock.release();
			throw new StoreException("Unable to get statements", e);
		}
		catch (RuntimeException e) {
			readLock.release();
			throw e;
		}
	}

	public long size(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		Lock readLock = nativeStore.getReadLock();

		try {
			ValueStore valueStore = nativeStore.getValueStore();

			int subjID = NativeValue.UNKNOWN_ID;
			if (subj != null) {
				subjID = valueStore.getID(subj);
				if (subjID == NativeValue.UNKNOWN_ID) {
					return 0;
				}
			}
			int predID = NativeValue.UNKNOWN_ID;
			if (pred != null) {
				predID = valueStore.getID(pred);
				if (predID == NativeValue.UNKNOWN_ID) {
					return 0;
				}
			}
			int objID = NativeValue.UNKNOWN_ID;
			if (obj != null) {
				objID = valueStore.getID(obj);
				if (objID == NativeValue.UNKNOWN_ID) {
					return 0;
				}
			}
			List<Integer> contextIDs;
			if (contexts != null && contexts.length == 0) {
				contextIDs = Arrays.asList(NativeValue.UNKNOWN_ID);
			}
			else {
				contextIDs = nativeStore.getContextIDs(OpenRDFUtil.notNull(contexts));
			}

			long size = 0L;

			for (int contextID : contextIDs) {
				// Iterate over all explicit statements
				RecordIterator iter = nativeStore.getTripleStore().getTriples(subjID, predID, objID, contextID,
						!includeInferred, transactionActive());

				try {
					while (iter.next() != null) {
						size++;
					}
				}
				finally {
					iter.close();
				}
			}

			return size;
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			readLock.release();
		}
	}

	public Cursor<? extends Namespace> getNamespaces()
		throws StoreException
	{
		Lock readLock = nativeStore.getReadLock();
		try {
			return new LockingCursor<NamespaceImpl>(readLock, new IteratorCursor<NamespaceImpl>(
					nativeStore.getNamespaceStore().iterator()));
		}
		catch (RuntimeException e) {
			readLock.release();
			throw e;
		}
	}

	public String getNamespace(String prefix)
		throws StoreException
	{
		Lock readLock = nativeStore.getReadLock();
		try {
			return nativeStore.getNamespaceStore().getNamespace(prefix);
		}
		finally {
			readLock.release();
		}
	}

	@Override
	public void begin()
		throws StoreException
	{
		txnLock = nativeStore.getTransactionLock();

		try {
			nativeStore.getTripleStore().startTransaction();
			super.begin();
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
	}

	@Override
	public void commit()
		throws StoreException
	{
		Lock storeReadLock = nativeStore.getReadLock();

		try {
			nativeStore.getValueStore().sync();
			nativeStore.getTripleStore().commit();
			nativeStore.getNamespaceStore().sync();

			txnLock.release();
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			storeReadLock.release();
		}

		nativeStore.notifySailChanged(sailChangedEvent);

		// create a fresh event object.
		sailChangedEvent = new DefaultSailChangedEvent(nativeStore);
		super.commit();
	}

	@Override
	public void rollback()
		throws StoreException
	{
		Lock storeReadLock = nativeStore.getReadLock();

		try {
			nativeStore.getValueStore().sync();
			nativeStore.getTripleStore().rollback();
			super.rollback();
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			txnLock.release();
			storeReadLock.release();
		}
	}

	public void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		addStatement(subj, pred, obj, true, contexts);
	}

	public boolean addInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		return addStatement(subj, pred, obj, false, contexts);
	}

	private boolean addStatement(Resource subj, URI pred, Value obj, boolean explicit, Resource... contexts)
		throws StoreException
	{
		boolean result = false;

		try {
			ValueStore valueStore = nativeStore.getValueStore();
			int subjID = valueStore.storeValue(subj);
			int predID = valueStore.storeValue(pred);
			int objID = valueStore.storeValue(obj);

			if (contexts != null && contexts.length == 0) {
				contexts = new Resource[] { null };
			}

			for (Resource context : OpenRDFUtil.notNull(contexts)) {
				int contextID = 0;
				if (context != null) {
					contextID = valueStore.storeValue(context);
				}

				boolean wasNew = nativeStore.getTripleStore().storeTriple(subjID, predID, objID, contextID,
						explicit);
				result |= wasNew;

				if (wasNew) {
					// The triple was not yet present in the triple store
					sailChangedEvent.setStatementsAdded(true);

					if (hasConnectionListeners()) {
						Statement st;

						if (context != null) {
							st = valueStore.createStatement(subj, pred, obj, context);
						}
						else {
							st = valueStore.createStatement(subj, pred, obj);
						}

						notifyStatementAdded(st);
					}
				}
			}
		}
		catch (IOException e) {
			throw new StoreException(e);
		}

		return result;
	}

	public void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		removeStatements(subj, pred, obj, true, contexts);
	}

	public boolean removeInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		int removeCount = removeStatements(subj, pred, obj, false, contexts);
		return removeCount > 0;
	}

	private int removeStatements(Resource subj, URI pred, Value obj, boolean explicit, Resource... contexts)
		throws StoreException
	{
		try {
			TripleStore tripleStore = nativeStore.getTripleStore();
			ValueStore valueStore = nativeStore.getValueStore();

			int subjID = NativeValue.UNKNOWN_ID;
			if (subj != null) {
				subjID = valueStore.getID(subj);
				if (subjID == NativeValue.UNKNOWN_ID) {
					return 0;
				}
			}
			int predID = NativeValue.UNKNOWN_ID;
			if (pred != null) {
				predID = valueStore.getID(pred);
				if (predID == NativeValue.UNKNOWN_ID) {
					return 0;
				}
			}
			int objID = NativeValue.UNKNOWN_ID;
			if (obj != null) {
				objID = valueStore.getID(obj);
				if (objID == NativeValue.UNKNOWN_ID) {
					return 0;
				}
			}

			contexts = OpenRDFUtil.notNull(contexts);
			List<Integer> contextIDList = new ArrayList<Integer>(contexts.length);
			if (contexts.length == 0) {
				contextIDList.add(NativeValue.UNKNOWN_ID);
			}
			else {
				for (Resource context : contexts) {
					if (context == null) {
						contextIDList.add(0);
					}
					else {
						int contextID = valueStore.getID(context);
						if (contextID != NativeValue.UNKNOWN_ID) {
							contextIDList.add(contextID);
						}
					}
				}
			}

			int removeCount = 0;

			for (int i = 0; i < contextIDList.size(); i++) {
				int contextID = contextIDList.get(i);

				List<Statement> removedStatements = Collections.emptyList();

				if (hasConnectionListeners()) {
					// We need to iterate over all matching triples so that they can
					// be reported
					RecordIterator btreeIter = tripleStore.getTriples(subjID, predID, objID, contextID, explicit,
							true);

					NativeStatementCursor iter = new NativeStatementCursor(btreeIter, valueStore);

					removedStatements = new ArrayList<Statement>();
					Statement st;
					while ((st = iter.next()) != null) {
						removedStatements.add(st);
					}
				}

				removeCount += tripleStore.removeTriples(subjID, predID, objID, contextID, explicit);

				for (Statement st : removedStatements) {
					notifyStatementRemoved(st);
				}
			}

			if (removeCount > 0) {
				sailChangedEvent.setStatementsRemoved(true);
			}

			return removeCount;
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
	}

	public void flushUpdates() {
		// no-op; changes are reported as soon as they come in
	}

	public void setNamespace(String prefix, String name)
		throws StoreException
	{
		nativeStore.getNamespaceStore().setNamespace(prefix, name);
	}

	public void removeNamespace(String prefix)
		throws StoreException
	{
		nativeStore.getNamespaceStore().removeNamespace(prefix);
	}

	public void clearNamespaces()
		throws StoreException
	{
		nativeStore.getNamespaceStore().clear();
	}
}