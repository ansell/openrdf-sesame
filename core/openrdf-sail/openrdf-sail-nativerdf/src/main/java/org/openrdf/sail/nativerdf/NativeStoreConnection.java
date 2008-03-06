/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
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

import info.aduna.concurrent.locks.Lock;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;
import info.aduna.iteration.DistinctIteration;
import info.aduna.iteration.FilterIteration;
import info.aduna.iteration.IteratorIteration;
import info.aduna.iteration.LockingIteration;

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
import org.openrdf.sail.nativerdf.btree.BTreeIterator;
import org.openrdf.sail.nativerdf.model.NativeValue;

/**
 * 
 */
class NativeStoreConnection extends SailConnectionBase {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final int ADD_STATEMENT_OP = 1;

	private static final int REMOVE_STATEMENT_OP = 2;

	private static final int REMOVE_NAMED_CONTEXT_STATEMENT_OP = 3;

	private static final int REMOVE_NULL_CONTEXT_STATEMENT_OP = 4;

	private static final int CLEAR_CONTEXT_OP = 5;

	private static final int CLEAR_OP = 6;

	private static final int SET_PREFIX_OP = 7;

	private static final int REMOVE_PREFIX_OP = 8;

	/*-----------*
	 * Variables *
	 *-----------*/

	private NativeStore _nativeStore;

	private DefaultSailChangedEvent _sailChangedEvent;

	private Lock _txnLock;

	private File _txnFile;

	private DataOutputStream _txnOutStream;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NativeStoreConnection(NativeStore nativeStore)
		throws IOException
	{
		_nativeStore = nativeStore;
		_sailChangedEvent = new DefaultSailChangedEvent(nativeStore);
	}

	/*---------*
	 * Methods *
	 *---------*/

	protected CloseableIteration<? extends BindingSet, QueryEvaluationException> _evaluate(TupleExpr tupleExpr,
			BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		Lock readLock = _nativeStore.getReadLock();

		try {
			_replaceValues(tupleExpr);

			TripleSource tripleSource = new NativeTripleSource(_nativeStore, includeInferred);
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

	private void _replaceValues(TupleExpr tupleExpr) throws SailException {
		// Replace all Value objects stored in variables with NativeValue objects,
		// which cache internal IDs
		tupleExpr.visit(new QueryModelVisitorBase<SailException>() {

			@Override
			public void meet(Var var)
			{
				if (var.hasValue()) {
					var.setValue(_nativeStore._valueStore.getNativeValue(var.getValue()));
				}
			}
		});
	}

	protected CloseableIteration<? extends Resource, SailException> _getContextIDs()
		throws SailException
	{
		// Which resources are used as context identifiers is not stored
		// separately. Iterate over all statements and extract their context.
		NativeTripleSource nts = new NativeTripleSource(_nativeStore, true);

		Lock readLock = _nativeStore.getReadLock();
		try {
			// Iterator over all statements
			CloseableIteration<? extends Statement, SailException> stIter = nts.getStatementsSailException(null, null, null);

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

	protected CloseableIteration<? extends Statement, SailException> _getStatements(Resource subj, URI pred,
			Value obj, boolean includeInferred, Resource... contexts)
		throws SailException
	{
		NativeTripleSource nts = new NativeTripleSource(_nativeStore, includeInferred);
		Lock readLock = _nativeStore.getReadLock();
		try {
			return new LockingIteration<Statement, SailException>(readLock, nts.getStatementsSailException(subj, pred, obj, contexts));
		}
		catch (RuntimeException e) {
			readLock.release();
			throw e;
		}
	}

	protected long _size(Resource... contexts)
		throws SailException
	{

		long size = 0L;

		Lock readLock = _nativeStore.getReadLock();

		BTreeIterator iter = null;

		try {
			if (contexts.length == 0) { // no context specified, count entire
				// store.
				try {
					iter = _nativeStore.getTripleStore().getTriples(-1, -1, -1, -1);
					while (iter.next() != null) {
						size++;
					}
				}
				finally {
					iter.close();
				}
			}
			else { // iterate over each specified context and increment size
				int contextID = -1;
				for (Resource context : contexts) {
					if (context == null) {
						contextID = 0;
					}
					else {
						contextID = _nativeStore.getValueStore().getID(context);
					}
					try {
						iter = _nativeStore.getTripleStore().getTriples(-1, -1, -1, contextID);
						while (iter.next() != null) {
							size++;
						}
					}
					finally {
						iter.close();
					}
				}
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

	protected CloseableIteration<? extends Namespace, SailException> _getNamespaces()
		throws SailException
	{
		Lock readLock = _nativeStore.getReadLock();
		try {
			return new LockingIteration<NamespaceImpl, SailException>(readLock, new IteratorIteration<NamespaceImpl, SailException>(_nativeStore._namespaceStore.iterator()));
		}
		catch (RuntimeException e) {
			readLock.release();
			throw e;
		}
	}

	protected String _getNamespace(String prefix)
		throws SailException
	{
		Lock readLock = _nativeStore.getReadLock();
		try {
			return _nativeStore._namespaceStore.getNamespace(prefix);
		}
		finally {
			readLock.release();
		}
	}

	protected void _startTransaction()
		throws SailException
	{
		try {
			_txnFile = File.createTempFile("txn", ".dat", _nativeStore.getDataDir());

			try {
				_txnOutStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(_txnFile),
						4096));

				try {
					_txnLock = _nativeStore.getTxnLock();
				}
				catch (InterruptedException e) {
					_txnOutStream.close();
					_txnFile.delete();
					throw new SailException("Failed to acquire transaction lock", e);
				}
			}
			catch (IOException e) {
				throw new SailException("Unable to open transaction file: " + _txnFile.getAbsolutePath());
			}
		}
		catch (IOException e) {
			throw new SailException("Unable to create transaction file in directory: "
					+ _nativeStore.getDataDir().getAbsolutePath());
		}
	}

	protected void _commit()
		throws SailException
	{
		Lock writeLock = _nativeStore.getWriteLock();

		try {
			_txnOutStream.close();
			_nativeStore.getValueStore().sync();

			_processTxnFile();

			_nativeStore.getTripleStore().sync();
			_nativeStore.getNamespaceStore().sync();

			_txnLock.release();
			_txnFile.delete();
		}
		catch (IOException e) {
			throw new SailException(e);
		}
		finally {
			writeLock.release();
		}

		_nativeStore.notifySailChanged(_sailChangedEvent);

		// create a fresh event object.
		_sailChangedEvent = new DefaultSailChangedEvent(_nativeStore);
	}

	protected void _rollback()
		throws SailException
	{
		try {
			_txnOutStream.close();
			_txnFile.delete();

			_nativeStore.getValueStore().sync();
		}
		catch (IOException e) {
			throw new SailException(e);
		}
		finally {
			_txnLock.release();
		}
	}

	private void _processTxnFile()
		throws SailException, IOException
	{
		// FIXME: acquire a write lock on the native store?
		DataInputStream log = new DataInputStream(new BufferedInputStream(new FileInputStream(_txnFile), 4096));

		try {
			int op = -1;
			while ((op = log.read()) != -1) {
				switch (op) {
					case ADD_STATEMENT_OP:
						_addStatement(log);
						break;
					case REMOVE_STATEMENT_OP:
						_removeStatements(log);
						break;
					case REMOVE_NULL_CONTEXT_STATEMENT_OP:
						_removeNullContextStatements(log);
						break;
					case REMOVE_NAMED_CONTEXT_STATEMENT_OP:
						_removeNamedContextStatements(log);
						break;
					case CLEAR_CONTEXT_OP:
						_clearContext(log);
						break;
					case CLEAR_OP:
						_clear(log);
						break;
					case SET_PREFIX_OP:
						_setNamespace(log);
						break;
					case REMOVE_PREFIX_OP:
						_removeNamespace(log);
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

	protected void _addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		try {
			ValueStore valueStore = _nativeStore.getValueStore();
			int subjID = valueStore.storeValue(subj);
			int predID = valueStore.storeValue(pred);
			int objID = valueStore.storeValue(obj);
			if (contexts.length > 0) {
				int[] contextIDs = new int[contexts.length];
				for (int i = 0; i < contexts.length; i++) {
					if (contexts[i] != null) {
						contextIDs[i] = valueStore.storeValue(contexts[i]);
					}
					else {
						contextIDs[i] = 0;
					}
				}

				for (int contextID : contextIDs) {
					_txnOutStream.write(ADD_STATEMENT_OP);
					_txnOutStream.writeInt(subjID);
					_txnOutStream.writeInt(predID);
					_txnOutStream.writeInt(objID);
					_txnOutStream.writeInt(contextID);
				}
			}
			else {// no context
				_txnOutStream.write(ADD_STATEMENT_OP);
				_txnOutStream.writeInt(subjID);
				_txnOutStream.writeInt(predID);
				_txnOutStream.writeInt(objID);
				_txnOutStream.writeInt(0);
			}
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	private void _addStatement(DataInputStream log)
		throws IOException
	{
		int subjID = log.readInt();
		int predID = log.readInt();
		int objID = log.readInt();
		int contextID = log.readInt();

		byte[] oldValue = _nativeStore.getTripleStore().storeTriple(subjID, predID, objID, contextID);

		if (oldValue == null) {
			// The triple was not yet present in the triple store
			_sailChangedEvent.setStatementsAdded(true);

			if (!_txnListeners.isEmpty()) {
				ValueStore valueStore = _nativeStore.getValueStore();

				Resource subj = (Resource)valueStore.getValue(subjID);
				URI pred = (URI)valueStore.getValue(predID);
				Value obj = valueStore.getValue(objID);
				Resource context = null;
				if (contextID != 0) {
					context = (Resource)valueStore.getValue(contextID);
				}

				Statement st = valueStore.createStatement(subj, pred, obj, context);
				_notifyStatementAdded(st);
			}
		}
	}

	protected void _removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		try {
			ValueStore valueStore = _nativeStore.getValueStore();
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

			if (contexts.length > 0) {
				int[] contextIDs = new int[contexts.length];

				for (int i = 0; i < contexts.length; i++) {
					contextIDs[i] = valueStore.storeValue(contexts[i]);
				}

				for (int contextID : contextIDs) {
					_txnOutStream.write(REMOVE_NAMED_CONTEXT_STATEMENT_OP);
					_txnOutStream.writeInt(subjID);
					_txnOutStream.writeInt(predID);
					_txnOutStream.writeInt(objID);
					_txnOutStream.writeInt(contextID);
				}
			}
			else {
				_txnOutStream.write(REMOVE_STATEMENT_OP);
				_txnOutStream.writeInt(subjID);
				_txnOutStream.writeInt(predID);
				_txnOutStream.writeInt(objID);
			}
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	private void _removeStatements(DataInputStream log)
		throws IOException
	{
		int subjID = log.readInt();
		int predID = log.readInt();
		int objID = log.readInt();

		int contextID = NativeValue.UNKNOWN_ID;

		int count = _nativeStore.getTripleStore().removeTriples(subjID, predID, objID, contextID);

		if (count > 0) {
			_sailChangedEvent.setStatementsRemoved(true);

			if (!_txnListeners.isEmpty()) {
				ValueStore valueStore = _nativeStore.getValueStore();

				Resource subj = (Resource)valueStore.getValue(subjID);
				URI pred = (URI)valueStore.getValue(predID);
				Value obj = valueStore.getValue(objID);
				Resource context = null;
				if (contextID != 0) {
					context = (Resource)valueStore.getValue(contextID);
				}

				Statement st = valueStore.createStatement(subj, pred, obj, context);

				_notifyStatementRemoved(st);
			}
		}
	}

	private void _removeNullContextStatements(DataInputStream log)
		throws IOException
	{
		int subjID = log.readInt();
		int predID = log.readInt();
		int objID = log.readInt();

		// FIXME correct for null context?
		int contextID = 0;

		int count = _nativeStore.getTripleStore().removeTriples(subjID, predID, objID, contextID);

		if (count > 0) {
			_sailChangedEvent.setStatementsRemoved(true);

			if (!_txnListeners.isEmpty()) {
				ValueStore valueStore = _nativeStore.getValueStore();

				Resource subj = (Resource)valueStore.getValue(subjID);
				URI pred = (URI)valueStore.getValue(predID);
				Value obj = valueStore.getValue(objID);
				Resource context = null;
				if (contextID != 0) {
					context = (Resource)valueStore.getValue(contextID);
				}

				Statement st = valueStore.createStatement(subj, pred, obj, context);

				_notifyStatementRemoved(st);
			}
		}
	}

	private void _removeNamedContextStatements(DataInputStream log)
		throws IOException
	{
		int subjID = log.readInt();
		int predID = log.readInt();
		int objID = log.readInt();
		int contextID = log.readInt();

		int count = _nativeStore.getTripleStore().removeTriples(subjID, predID, objID, contextID);

		if (count > 0) {
			_sailChangedEvent.setStatementsRemoved(true);

			if (!_txnListeners.isEmpty()) {
				ValueStore valueStore = _nativeStore.getValueStore();

				Resource subj = (Resource)valueStore.getValue(subjID);
				URI pred = (URI)valueStore.getValue(predID);
				Value obj = valueStore.getValue(objID);
				Resource context = null;
				if (contextID != 0) {
					context = (Resource)valueStore.getValue(contextID);
				}

				Statement st = valueStore.createStatement(subj, pred, obj, context);

				_notifyStatementRemoved(st);
			}
		}
	}

	protected void _clear(Resource... contexts)
		throws SailException
	{
		try {
			// TODO: should we discard any previously written operations, or do
			// we assume that clear() will only be called as the first operation?
			if (contexts.length == 0) {
				_txnOutStream.write(CLEAR_OP);
			}
			else {
				_removeStatements(null, null, null, contexts);
			}
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	private void _clear(DataInputStream log)
		throws IOException
	{
		_nativeStore.getTripleStore().clear();
		_nativeStore.getValueStore().clear();
		_nativeStore.getNamespaceStore().clear();

		_sailChangedEvent.setStatementsRemoved(true);
	}

	protected void _clearContext(Resource context)
		throws SailException
	{
		try {
			int contextID = 0;
			if (context != null) {
				contextID = _nativeStore.getValueStore().storeValue(context);
			}

			_txnOutStream.write(CLEAR_CONTEXT_OP);
			_txnOutStream.writeInt(contextID);
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	private void _clearContext(DataInputStream log)
		throws IOException
	{
		int contextID = log.readInt();

		int count = _nativeStore.getTripleStore().removeTriples(-1, -1, -1, contextID);

		if (count > 0) {
			_sailChangedEvent.setStatementsRemoved(true);
		}
	}

	protected void _setNamespace(String prefix, String name)
		throws SailException
	{
		try {
			_txnOutStream.write(SET_PREFIX_OP);
			_txnOutStream.writeUTF(name);
			_txnOutStream.writeUTF(prefix);
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	private void _setNamespace(DataInputStream log)
		throws IOException
	{
		String namespace = log.readUTF();
		String prefix = log.readUTF();
		_nativeStore.getNamespaceStore().setNamespace(prefix, namespace);
	}

	protected void _removeNamespace(String prefix)
		throws SailException
	{
		try {
			_txnOutStream.write(REMOVE_PREFIX_OP);
			_txnOutStream.writeUTF(prefix);
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	private void _removeNamespace(DataInputStream log)
		throws IOException
	{
		String prefix = log.readUTF();
		_nativeStore.getNamespaceStore().removeNamespace(prefix);
	}
}
