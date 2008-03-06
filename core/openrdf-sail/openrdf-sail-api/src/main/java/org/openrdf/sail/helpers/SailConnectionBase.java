/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;

/**
 * Class offering base functionality for SailConnection implementations.
 * 
 * @author Arjohn Kampman
 */
public abstract class SailConnectionBase implements SailConnection {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

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

	public final boolean isOpen()
		throws SailException
	{
		return _isOpen;
	}

	protected void _verifyIsOpen()
		throws SailException
	{
		if (!_isOpen) {
			throw new IllegalStateException("Connection has been closed");
		}
	}

	public final void close()
		throws SailException
	{
		if (_isOpen) {
			if (_txnActive) {
				logger.warn("Rolling back transaction due to connection close");
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

	public final CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(TupleExpr tupleExpr, BindingSet bindings,
			boolean includeInferred)
		throws SailException
	{
		_verifyIsOpen();
		return _evaluate(tupleExpr, bindings, includeInferred);
	}

	protected abstract CloseableIteration<? extends BindingSet, QueryEvaluationException> _evaluate(TupleExpr tupleExpr,
			BindingSet bindings, boolean includeInferred)
		throws SailException;

	public final CloseableIteration<? extends Resource, SailException> getContextIDs()
		throws SailException
	{
		_verifyIsOpen();
		return _getContextIDs();
	}

	protected abstract CloseableIteration<? extends Resource, SailException> _getContextIDs()
		throws SailException;

	public final CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, URI pred, Value obj,
			boolean useInference, Resource... contexts)
		throws SailException
	{
		_verifyIsOpen();
		return _getStatements(subj, pred, obj, useInference, contexts);
	}

	protected abstract CloseableIteration<? extends Statement, SailException> _getStatements(Resource subj, URI pred,
			Value obj, boolean useInference, Resource... contexts)
		throws SailException;

	public final long size(Resource... contexts)
		throws SailException
	{
		_verifyIsOpen();
		return _size(contexts);
	}

	protected abstract long _size(Resource... contexts)
		throws SailException;

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

	public final void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		_autoStartTransaction();
		_addStatement(subj, pred, obj, contexts);
	}

	protected abstract void _addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException;

	public final void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		_autoStartTransaction();
		_removeStatements(subj, pred, obj, contexts);
	}

	protected abstract void _removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException;

	public final void clear(Resource... contexts)
		throws SailException
	{
		_autoStartTransaction();
		_clear(contexts);
	}

	protected abstract void _clear(Resource... contexts)
		throws SailException;

	public final CloseableIteration<? extends Namespace, SailException> getNamespaces()
		throws SailException
	{
		_verifyIsOpen();
		return _getNamespaces();
	}

	protected abstract CloseableIteration<? extends Namespace, SailException> _getNamespaces()
		throws SailException;

	public final String getNamespace(String prefix)
		throws SailException
	{
		_verifyIsOpen();
		return _getNamespace(prefix);
	}

	protected abstract String _getNamespace(String prefix)
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
