/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.querymodel.GraphQuery;
import org.openrdf.querymodel.TupleQuery;
import org.openrdf.queryresult.GraphQueryResult;
import org.openrdf.queryresult.TupleQueryResult;
import org.openrdf.sail.Namespace;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;
import org.openrdf.util.iterator.CloseableIterator;
import org.openrdf.util.log.ThreadLog;

/**
 * Class offering base functionality for SailConnection implementations.
 */
public abstract class SailConnectionBase implements SailConnection {

	/*-----------*
	 * Variables *
	 *-----------*/

	protected List<SailConnectionListener> _txnListeners;

	private boolean _isOpen;

	private boolean _txnActive;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SailConnectionBase() {
		_txnListeners = new ArrayList<SailConnectionListener>(0);
		_isOpen = true;
		_txnActive = false;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public final boolean isOpen() {
		return _isOpen;
	}

	protected void _verifyIsOpen() {
		if (!_isOpen) {
			throw new IllegalStateException("Connection has been closed");
		}
	}

	public final void close()
		throws SailException
	{
		if (_isOpen) {
			if (_txnActive) {
				ThreadLog.warning("Rolling back transaction due to connection close");
				rollback();
			}

			_close();

			_isOpen = false;
		}
	}

	protected void _close()
		throws SailException
	{
	}

	public final TupleQueryResult evaluate(TupleQuery query, boolean includeInferred) {
		_verifyIsOpen();
		return _evaluate(query, includeInferred);
	}

	protected abstract TupleQueryResult _evaluate(TupleQuery query, boolean includeInferred);

	public final GraphQueryResult evaluate(GraphQuery query, boolean includeInferred) {
		_verifyIsOpen();
		return _evaluate(query, includeInferred);
	}

	protected abstract GraphQueryResult _evaluate(GraphQuery query, boolean includeInferred);

	public final CloseableIterator<? extends Resource> getContextIDs() {
		_verifyIsOpen();
		return _getContextIDs();
	}

	protected abstract CloseableIterator<? extends Resource> _getContextIDs();

	public final CloseableIterator<? extends Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean useInference)
	{
		_verifyIsOpen();
		return _getStatements(subj, pred, obj, useInference);
	}

	protected abstract CloseableIterator<? extends Statement> _getStatements(Resource subj, URI pred,
			Value obj, boolean useInference);

	public final CloseableIterator<? extends Statement> getNullContextStatements(Resource subj, URI pred,
			Value obj, boolean useInference)
	{
		_verifyIsOpen();
		return _getNullContextStatements(subj, pred, obj, useInference);
	}

	protected abstract CloseableIterator<? extends Statement> _getNullContextStatements(Resource subj,
			URI pred, Value obj, boolean useInference);

	public final CloseableIterator<? extends Statement> getNamedContextStatements(Resource subj, URI pred,
			Value obj, Resource context, boolean useInference)
	{
		_verifyIsOpen();
		return _getNamedContextStatements(subj, pred, obj, context, useInference);
	}

	protected abstract CloseableIterator<? extends Statement> _getNamedContextStatements(Resource subj,
			URI pred, Value obj, Resource context, boolean useInference);

	public final CloseableIterator<? extends Namespace> getNamespaces() {
		_verifyIsOpen();
		return _getNamespaces();
	}

	protected abstract CloseableIterator<? extends Namespace> _getNamespaces();

	protected void _autoStartTransaction()
		throws SailException
	{
		if (!_txnActive) {
			_verifyIsOpen();

			_startTransaction();

			_txnActive = true;
		}
	}

	protected abstract void _startTransaction()
		throws SailException;

	protected final boolean txnStarted() {
		return _txnActive;
	}

	public final void commit()
		throws SailException
	{
		_verifyIsOpen();

		if (_txnActive) {
			_commit();
			_txnActive = false;
		}
	}

	protected abstract void _commit()
		throws SailException;

	public final void rollback()
		throws SailException
	{
		_verifyIsOpen();

		if (_txnActive) {
			try {
				_rollback();
			}
			finally {
				_txnActive = false;
			}
		}
	}

	protected abstract void _rollback()
		throws SailException;

	public final void addStatement(Resource subj, URI pred, Value obj, Resource context)
		throws SailException
	{
		_autoStartTransaction();
		_addStatement(subj, pred, obj, context);
	}

	protected abstract void _addStatement(Resource subj, URI pred, Value obj, Resource context)
		throws SailException;

	public final void removeStatement(Resource subj, URI pred, Value obj, Resource context)
		throws SailException
	{
		_autoStartTransaction();
		_removeStatement(subj, pred, obj, context);
	}

	protected abstract void _removeStatement(Resource subj, URI pred, Value obj, Resource context)
		throws SailException;

	public final void clear()
		throws SailException
	{
		_autoStartTransaction();
		_clear();
	}

	protected abstract void _clear()
		throws SailException;

	public final void clearContext(Resource context)
		throws SailException
	{
		_autoStartTransaction();
		_clearContext(context);
	}

	protected abstract void _clearContext(Resource context)
		throws SailException;

	public final void setNamespace(String prefix, String name)
		throws SailException
	{
		_autoStartTransaction();
		_setNamespace(prefix, name);
	}

	protected abstract void _setNamespace(String prefix, String name)
		throws SailException;

	public final void removeNamespace(String prefix)
		throws SailException
	{
		_autoStartTransaction();
		_removeNamespace(prefix);
	}

	protected abstract void _removeNamespace(String prefix)
		throws SailException;

	public void addConnectionListener(SailConnectionListener listener) {
		synchronized (_txnListeners) {
			_txnListeners.add(listener);
		}
	}

	public void removeConnectionListener(SailConnectionListener listener) {
		synchronized (_txnListeners) {
			_txnListeners.remove(listener);
		}
	}

	protected void _notifyStatementAdded(Statement st) {
		synchronized (_txnListeners) {
			for (SailConnectionListener listener : _txnListeners) {
				listener.statementAdded(st);
			}
		}
	}

	protected void _notifyStatementRemoved(Statement st) {
		synchronized (_txnListeners) {
			for (SailConnectionListener listener : _txnListeners) {
				listener.statementRemoved(st);
			}
		}
	}
}
