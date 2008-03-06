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

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.querylogic.EvaluationStrategy;
import org.openrdf.querylogic.QuerySolution;
import org.openrdf.querylogic.impl.EvaluationStrategyImpl;
import org.openrdf.querymodel.GraphQuery;
import org.openrdf.querymodel.QueryOptimizer;
import org.openrdf.querymodel.TupleExpr;
import org.openrdf.querymodel.TupleQuery;
import org.openrdf.querymodel.Var;
import org.openrdf.querymodel.helpers.QueryModelVisitorBase;
import org.openrdf.queryresult.GraphQueryResult;
import org.openrdf.queryresult.Solution;
import org.openrdf.queryresult.TupleQueryResult;
import org.openrdf.sail.Namespace;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.helpers.NamespaceImpl;
import org.openrdf.sail.helpers.SailConnectionBase;
import org.openrdf.sail.helpers.SailGraphQueryResult;
import org.openrdf.sail.helpers.SailTupleQueryResult;
import org.openrdf.util.iterator.CloseableIterator;
import org.openrdf.util.iterator.ConvertingIterator;
import org.openrdf.util.iterator.DistinctIterator;
import org.openrdf.util.iterator.FilterIterator;
import org.openrdf.util.iterator.LockingIterator;
import org.openrdf.util.locking.Lock;

/**
 * 
 */
class NativeStoreConnection extends SailConnectionBase {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final int ADD_STATEMENT_OP = 1;

	private static final int REMOVE_STATEMENT_OP = 2;

	private static final int CLEAR_CONTEXT_OP = 3;

	private static final int CLEAR_OP = 4;

	private static final int SET_PREFIX_OP = 5;

	private static final int REMOVE_PREFIX_OP = 6;

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

	protected TupleQueryResult _evaluate(TupleQuery query, boolean includeInferred) {
		return _evaluate(query.getTupleExpr(), includeInferred);
	}

	protected GraphQueryResult _evaluate(GraphQuery query, boolean includeInferred) {
		final TupleQueryResult result = _evaluate(query.getTupleExpr(), includeInferred);

		CloseableIterator<Statement> iter = new ConvertingIterator<Solution, Statement>(result.iterator()) {

			protected Statement convert(Solution solution) {
				Resource subject = (Resource)solution.getValue("subject");
				URI predicate = (URI)solution.getValue("predicate");
				Value object = solution.getValue("object");
				Resource context = (Resource)solution.getValue("context");

				if (context == null) {
					return _nativeStore.getValueFactory().createStatement(subject, predicate, object);
				}
				else {
					return _nativeStore.getValueFactory().createStatement(subject, predicate, object, context);
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

		// Replace all Value objects stored in variables with NativeValue objects
		tupleExpr.visit(new QueryModelVisitorBase() {

			@Override
			public void meet(Var var)
			{
				if (var.hasValue()) {
					var.setValue(_nativeStore._valueStore.getNativeValue(var.getValue()));
				}
			}
		});

		Lock lock = _nativeStore.getReadLock();
		try {
			EvaluationStrategy strategy = new EvaluationStrategyImpl();
			CloseableIterator<Solution> iter = strategy.evaluate(tupleExpr, new NativeTripleSource(_nativeStore,
					includeInferred), new QuerySolution());
			iter = new LockingIterator<Solution>(lock, iter);

			return new SailTupleQueryResult(tupleExpr.getBindingNames(), iter);
		}
		catch (RuntimeException e) {
			lock.release();
			throw e;
		}
	}

	protected CloseableIterator<? extends Resource> _getContextIDs() {
		// Which resources are used as context identifiers is not stored
		// separately. Iterate over all statements and extract their context.
		NativeTripleSource nts = new NativeTripleSource(_nativeStore, true);

		Lock lock = _nativeStore.getReadLock();
		try {
			// Iterator over all statements
			CloseableIterator<? extends Statement> stIter = nts.getStatements(null, null, null);

			// Filter statements without context resource
			stIter = new FilterIterator<Statement>(stIter) {

				protected boolean accept(Statement st) {
					return st.getContext() != null;
				}
			};

			// Return the contexts of the statements, filtering any duplicates,
			// releasing the read lock when the iterator is closed
			return new LockingIterator<Resource>(lock, new DistinctIterator<Resource>(
					new ConvertingIterator<Statement, Resource>(stIter) {

						protected Resource convert(Statement st) {
							return st.getContext();
						}
					}));
		}
		catch (RuntimeException e) {
			lock.release();
			throw e;
		}
	}

	protected CloseableIterator<? extends Statement> _getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred)
	{
		NativeTripleSource nts = new NativeTripleSource(_nativeStore, includeInferred);
		Lock lock = _nativeStore.getReadLock();
		try {
			return new LockingIterator<Statement>(lock, nts.getStatements(subj, pred, obj));
		}
		catch (RuntimeException e) {
			lock.release();
			throw e;
		}
	}

	protected CloseableIterator<? extends Statement> _getNullContextStatements(Resource subj, URI pred,
			Value obj, boolean includeInferred)
	{
		NativeTripleSource nts = new NativeTripleSource(_nativeStore, includeInferred);
		Lock lock = _nativeStore.getReadLock();
		try {
			return new LockingIterator<Statement>(lock, nts.getNullContextStatements(subj, pred, obj));
		}
		catch (RuntimeException e) {
			lock.release();
			throw e;
		}
	}

	protected CloseableIterator<? extends Statement> _getNamedContextStatements(Resource subj, URI pred,
			Value obj, Resource context, boolean includeInferred)
	{
		NativeTripleSource nts = new NativeTripleSource(_nativeStore, includeInferred);
		Lock lock = _nativeStore.getReadLock();
		try {
			return new LockingIterator<Statement>(lock, nts.getNamedContextStatements(subj, pred, obj, context));
		}
		catch (RuntimeException e) {
			lock.release();
			throw e;
		}
	}

	protected CloseableIterator<? extends Namespace> _getNamespaces() {
		Lock lock = _nativeStore.getReadLock();
		try {
			return new LockingIterator<NamespaceImpl>(lock, _nativeStore._namespaceStore.iterator());
		}
		catch (RuntimeException e) {
			lock.release();
			throw e;
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

		_nativeStore._notifySailChanged(_sailChangedEvent);

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
						_removeStatement(log);
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

	protected void _addStatement(Resource subj, URI pred, Value obj, Resource context)
		throws SailException
	{
		try {
			ValueStore valueStore = _nativeStore.getValueStore();
			int subjID = valueStore.storeValue(subj);
			int predID = valueStore.storeValue(pred);
			int objID = valueStore.storeValue(obj);
			int contextID = 0;
			if (context != null) {
				contextID = valueStore.storeValue(context);
			}

			_txnOutStream.write(ADD_STATEMENT_OP);
			_txnOutStream.writeInt(subjID);
			_txnOutStream.writeInt(predID);
			_txnOutStream.writeInt(objID);
			_txnOutStream.writeInt(contextID);
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

	protected void _removeStatement(Resource subj, URI pred, Value obj, Resource context)
		throws SailException
	{
		try {
			ValueStore valueStore = _nativeStore.getValueStore();
			int subjID = valueStore.storeValue(subj);
			int predID = valueStore.storeValue(pred);
			int objID = valueStore.storeValue(obj);
			int contextID = 0;
			if (context != null) {
				contextID = valueStore.storeValue(context);
			}

			_txnOutStream.write(REMOVE_STATEMENT_OP);
			_txnOutStream.writeInt(subjID);
			_txnOutStream.writeInt(predID);
			_txnOutStream.writeInt(objID);
			_txnOutStream.writeInt(contextID);
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	private void _removeStatement(DataInputStream log)
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

	protected void _clear()
		throws SailException
	{
		try {
			// TODO: should we discard any previously written operations, or do
			// we assume that clear() will only be called as the first operation?
			_txnOutStream.write(CLEAR_OP);
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
