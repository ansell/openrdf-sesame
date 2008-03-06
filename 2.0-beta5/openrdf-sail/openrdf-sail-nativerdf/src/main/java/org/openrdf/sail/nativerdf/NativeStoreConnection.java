/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import info.aduna.concurrent.locks.Lock;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;
import info.aduna.iteration.DistinctIteration;
import info.aduna.iteration.FilterIteration;
import info.aduna.iteration.IteratorIteration;
import info.aduna.iteration.LockingIteration;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.impl.BooleanExprOptimizer;
import org.openrdf.query.algebra.evaluation.impl.CostComparator;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.algebra.evaluation.impl.QueryJoinOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.helpers.SailConnectionBase;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.nativerdf.model.NativeValue;

/**
 * 
 */
class NativeStoreConnection extends SailConnectionBase implements InferencerConnection {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final int ADD_STATEMENT_OP = 1;

	private static final int REMOVE_STATEMENTS_OP = 2;

	private static final int CLEAR_OP = 3;

	private static final int SET_PREFIX_OP = 4;

	private static final int REMOVE_PREFIX_OP = 5;

	/*-----------*
	 * Variables *
	 *-----------*/

	private NativeStore nativeStore;

	private DefaultSailChangedEvent sailChangedEvent;

	private Lock txnLock;

	private File txnFile;

	private DataOutputStream txnOutStream;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NativeStoreConnection(NativeStore nativeStore)
		throws IOException
	{
		super(nativeStore);
		this.nativeStore = nativeStore;
		sailChangedEvent = new DefaultSailChangedEvent(nativeStore);
	}

	/*---------*
	 * Methods *
	 *---------*/

	protected void closeInternal() {
		// FIXME we should check for open iteration objects.
	}

	protected CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(
			TupleExpr tupleExpr, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		Lock readLock = nativeStore.getReadLock();

		try {
			replaceValues(tupleExpr);

			TripleSource tripleSource = new NativeTripleSource(nativeStore, includeInferred);
			EvaluationStrategyImpl strategy = new EvaluationStrategyImpl(tripleSource);

			QueryJoinOptimizer joinOptimizer = new QueryJoinOptimizer(new CostComparator());
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
			return new LockingIteration<BindingSet, QueryEvaluationException>(readLock, iter);
		}
		catch (RuntimeException e) {
			readLock.release();
			throw e;
		}
	}

	private void replaceValues(TupleExpr tupleExpr)
		throws SailException
	{
		// Replace all Value objects stored in variables with NativeValue objects,
		// which cache internal IDs
		tupleExpr.visit(new QueryModelVisitorBase<SailException>() {

			@Override
			public void meet(Var var)
			{
				if (var.hasValue()) {
					var.setValue(nativeStore.valueStore.getNativeValue(var.getValue()));
				}
			}
		});
	}

	protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal()
		throws SailException
	{
		// Which resources are used as context identifiers is not stored
		// separately. Iterate over all statements and extract their context.
		NativeTripleSource nts = new NativeTripleSource(nativeStore, true);

		Lock readLock = nativeStore.getReadLock();
		try {
			// Iterator over all statements
			CloseableIteration<? extends Statement, SailException> stIter = nts.getStatementsSailException(null,
					null, null);

			// Filter statements without context resource
			stIter = new FilterIteration<Statement, SailException>(stIter) {

				protected boolean accept(Statement st) {
					return st.getContext() != null;
				}
			};

			// Return the contexts of the statements, filtering any duplicates,
			// releasing the read lock when the iterator is closed
			return new LockingIteration<Resource, SailException>(readLock,
					new DistinctIteration<Resource, SailException>(
							new ConvertingIteration<Statement, Resource, SailException>(stIter) {

								protected Resource convert(Statement st) {
									return st.getContext();
								}
							}));
		}
		catch (RuntimeException e) {
			readLock.release();
			throw e;
		}
	}

	protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(Resource subj,
			URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws SailException
	{
		NativeTripleSource nts = new NativeTripleSource(nativeStore, includeInferred);

		Lock readLock = nativeStore.getReadLock();
		try {
			return new LockingIteration<Statement, SailException>(readLock, nts.getStatementsSailException(subj,
					pred, obj, contexts));
		}
		catch (RuntimeException e) {
			readLock.release();
			throw e;
		}
	}

	protected long sizeInternal(Resource... contexts)
		throws SailException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		Lock readLock = nativeStore.getReadLock();

		try {
			List<Integer> contextIDs;
			if (contexts.length == 0) {
				contextIDs = Arrays.asList(NativeValue.UNKNOWN_ID);
			}
			else {
				contextIDs = nativeStore.getContextIDs(contexts);
			}

			long size = 0L;

			for (int contextID : contextIDs) {
				size += nativeStore.getTripleStore().countTriples(-1, -1, -1, contextID, true);
			}

			return size;
		}
		catch (IOException e) {
			throw new SailException(e);
		}
		finally {
			readLock.release();
		}
	}

	protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal()
		throws SailException
	{
		Lock readLock = nativeStore.getReadLock();
		try {
			return new LockingIteration<NamespaceImpl, SailException>(readLock,
					new IteratorIteration<NamespaceImpl, SailException>(nativeStore.namespaceStore.iterator()));
		}
		catch (RuntimeException e) {
			readLock.release();
			throw e;
		}
	}

	protected String getNamespaceInternal(String prefix)
		throws SailException
	{
		Lock readLock = nativeStore.getReadLock();
		try {
			return nativeStore.namespaceStore.getNamespace(prefix);
		}
		finally {
			readLock.release();
		}
	}

	protected void startTransactionInternal()
		throws SailException
	{
		try {
			txnFile = File.createTempFile("txn", ".dat", nativeStore.getDataDir());

			try {
				txnOutStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(txnFile), 4096));

				try {
					txnLock = nativeStore.getTxnLock();
				}
				catch (InterruptedException e) {
					txnOutStream.close();
					txnFile.delete();
					throw new SailException("Failed to acquire transaction lock", e);
				}
			}
			catch (IOException e) {
				throw new SailException("Unable to open transaction file: " + txnFile.getAbsolutePath());
			}
		}
		catch (IOException e) {
			throw new SailException("Unable to create transaction file in directory: "
					+ nativeStore.getDataDir().getAbsolutePath());
		}
	}

	protected void commitInternal()
		throws SailException
	{
		Lock writeLock = nativeStore.getWriteLock();

		try {
			txnOutStream.close();
			nativeStore.getValueStore().sync();

			processTxnFile();

			nativeStore.getTripleStore().sync();
			nativeStore.getNamespaceStore().sync();

			txnLock.release();
			txnFile.delete();
		}
		catch (IOException e) {
			throw new SailException(e);
		}
		finally {
			writeLock.release();
		}

		nativeStore.notifySailChanged(sailChangedEvent);

		// create a fresh event object.
		sailChangedEvent = new DefaultSailChangedEvent(nativeStore);
	}

	protected void rollbackInternal()
		throws SailException
	{
		try {
			txnOutStream.close();
			txnFile.delete();

			nativeStore.getValueStore().sync();
		}
		catch (IOException e) {
			throw new SailException(e);
		}
		finally {
			txnLock.release();
		}
	}

	private void processTxnFile()
		throws SailException, IOException
	{
		// FIXME: acquire a write lock on the native store?
		DataInputStream log = new DataInputStream(new BufferedInputStream(new FileInputStream(txnFile), 4096));

		try {
			int op = -1;
			while ((op = log.read()) != -1) {
				switch (op) {
					case ADD_STATEMENT_OP:
						addStatement(log);
						break;
					case REMOVE_STATEMENTS_OP:
						removeStatements(log);
						break;
					case CLEAR_OP:
						clear(log);
						break;
					case SET_PREFIX_OP:
						setNamespace(log);
						break;
					case REMOVE_PREFIX_OP:
						removeNamespace(log);
						break;
					default:
						throw new SailException("Invalid operation type: " + op);
				}
			}
		}
		finally {
			log.close();
		}
	}

	protected void addStatementInternal(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		addStatement(subj, pred, obj, true, contexts);
	}

	private void addStatement(Resource subj, URI pred, Value obj, boolean explicit, Resource... contexts)
		throws SailException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		try {
			ValueStore valueStore = nativeStore.getValueStore();
			int subjID = valueStore.storeValue(subj);
			int predID = valueStore.storeValue(pred);
			int objID = valueStore.storeValue(obj);

			List<Integer> contextIDs;

			if (contexts.length == 0) {
				contextIDs = Arrays.asList(0);
			}
			else {
				contextIDs = nativeStore.storeContextIDs(contexts);
			}

			for (int contextID : contextIDs) {
				txnOutStream.write(ADD_STATEMENT_OP);
				txnOutStream.writeInt(subjID);
				txnOutStream.writeInt(predID);
				txnOutStream.writeInt(objID);
				txnOutStream.writeInt(contextID);
				txnOutStream.writeBoolean(explicit);
			}
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	private void addStatement(DataInputStream log)
		throws IOException
	{
		int subjID = log.readInt();
		int predID = log.readInt();
		int objID = log.readInt();
		int contextID = log.readInt();
		boolean explicit = log.readBoolean();

		byte[] oldValue = nativeStore.getTripleStore().storeTriple(subjID, predID, objID, contextID, explicit);

		if (oldValue == null) {
			// The triple was not yet present in the triple store
			sailChangedEvent.setStatementsAdded(true);

			if (hasConnectionListeners()) {
				ValueStore valueStore = nativeStore.getValueStore();

				Resource subj = (Resource)valueStore.getValue(subjID);
				URI pred = (URI)valueStore.getValue(predID);
				Value obj = valueStore.getValue(objID);

				Statement st;

				if (contextID != 0) {
					Resource context = (Resource)valueStore.getValue(contextID);
					st = valueStore.createStatement(subj, pred, obj, context);
				}
				else {
					st = valueStore.createStatement(subj, pred, obj);
				}

				notifyStatementAdded(st);
			}
		}
	}

	protected void removeStatementsInternal(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		removeStatements(subj, pred, obj, true, contexts);
	}

	private void removeStatements(Resource subj, URI pred, Value obj, boolean explicit, Resource... contexts)
		throws SailException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		try {
			ValueStore valueStore = nativeStore.getValueStore();
			int subjID = NativeValue.UNKNOWN_ID;
			if (subj != null) {
				subjID = valueStore.storeValue(subj);
			}
			int predID = NativeValue.UNKNOWN_ID;
			if (pred != null) {
				predID = valueStore.storeValue(pred);
			}
			int objID = NativeValue.UNKNOWN_ID;
			if (obj != null) {
				objID = valueStore.storeValue(obj);
			}

			List<Integer> contextIDs;
			if (contexts.length == 0) {
				contextIDs = Arrays.asList(NativeValue.UNKNOWN_ID);
			}
			else {
				contextIDs = nativeStore.storeContextIDs(contexts);
			}

			for (int contextID : contextIDs) {
				txnOutStream.write(REMOVE_STATEMENTS_OP);
				txnOutStream.writeInt(subjID);
				txnOutStream.writeInt(predID);
				txnOutStream.writeInt(objID);
				txnOutStream.writeInt(contextID);
				txnOutStream.writeBoolean(explicit);
			}
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	private void removeStatements(DataInputStream log)
		throws IOException
	{
		int subjID = log.readInt();
		int predID = log.readInt();
		int objID = log.readInt();
		int contextID = log.readInt();
		boolean explicit = log.readBoolean();

		int count = nativeStore.getTripleStore().removeTriples(subjID, predID, objID, contextID, explicit);

		if (count > 0) {
			sailChangedEvent.setStatementsRemoved(true);

			if (hasConnectionListeners()) {
				ValueStore valueStore = nativeStore.getValueStore();

				// FIXME: these values might be null, need to retrieve IDs from
				// triple store instead
				Resource subj = (Resource)valueStore.getValue(subjID);
				URI pred = (URI)valueStore.getValue(predID);
				Value obj = valueStore.getValue(objID);

				Statement st;

				if (contextID > 0) {
					Resource context = (Resource)valueStore.getValue(contextID);
					st = valueStore.createStatement(subj, pred, obj, context);
				}
				else {
					st = valueStore.createStatement(subj, pred, obj);
				}

				notifyStatementRemoved(st);
			}
		}
	}

	protected void clearInternal(Resource... contexts)
		throws SailException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		try {
			// TODO: should we discard any previously written operations, or do
			// we assume that clear() will only be called as the first operation?
			if (contexts.length == 0) {
				txnOutStream.write(CLEAR_OP);
			}
			else {
				removeStatementsInternal(null, null, null, contexts);
			}
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	private void clear(DataInputStream log)
		throws IOException
	{
		nativeStore.getTripleStore().clear();
		nativeStore.getValueStore().clear();
		nativeStore.getNamespaceStore().clear();

		sailChangedEvent.setStatementsRemoved(true);
	}

	protected void setNamespaceInternal(String prefix, String name)
		throws SailException
	{
		try {
			txnOutStream.write(SET_PREFIX_OP);
			txnOutStream.writeUTF(name);
			txnOutStream.writeUTF(prefix);
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	private void setNamespace(DataInputStream log)
		throws IOException
	{
		String namespace = log.readUTF();
		String prefix = log.readUTF();
		nativeStore.getNamespaceStore().setNamespace(prefix, namespace);
	}

	protected void removeNamespaceInternal(String prefix)
		throws SailException
	{
		try {
			txnOutStream.write(REMOVE_PREFIX_OP);
			txnOutStream.writeUTF(prefix);
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	private void removeNamespace(DataInputStream log)
		throws IOException
	{
		String prefix = log.readUTF();
		nativeStore.getNamespaceStore().removeNamespace(prefix);
	}

	public boolean addInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		CloseableIteration<? extends Statement, SailException> iter = getStatements(subj, pred, obj, true,
				contexts);
		boolean exists = iter.hasNext();
		iter.close();

		autoStartTransaction();
		addStatement(subj, pred, obj, false, contexts);
		return !exists;
	}

	public void clearInferred()
		throws SailException
	{
		removeStatements(null, null, null, false);
	}

	public void clearInferredFromContext(Resource... contexts)
		throws SailException
	{
		removeStatements(null, null, null, false, contexts);
	}

	public boolean removeInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		CloseableIteration<? extends Statement, SailException> iter = getStatements(subj, pred, obj, true,
				contexts);
		boolean exists = iter.hasNext();
		removeStatements(subj, pred, obj, false, contexts);
		return exists;
	}
}
