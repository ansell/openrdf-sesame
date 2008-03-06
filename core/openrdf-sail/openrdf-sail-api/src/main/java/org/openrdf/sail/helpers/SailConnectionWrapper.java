/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

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

/**
 * An implementation of the Transaction interface that wraps another Transaction
 * object and forwards any method calls to the wrapped transaction.
 * 
 * @author jeen
 */
public class SailConnectionWrapper implements SailConnection {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The wrapped SailConnection.
	 */
	private SailConnection _wrappedCon;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TransactionWrapper object that wraps the supplied
	 * connection.
	 */
	public SailConnectionWrapper(SailConnection wrappedCon) {
		_wrappedCon = wrappedCon;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the Connection that is wrapped by this object.
	 * 
	 * @return The SailConnection object that was supplied to the constructor of
	 *         this class.
	 */
	protected SailConnection getWrappedConnection() {
		return _wrappedCon;
	}

	public boolean isOpen() {
		return _wrappedCon.isOpen();
	}

	public void close()
		throws SailException
	{
		_wrappedCon.close();
	}

	public void commit()
		throws SailException
	{
		_wrappedCon.commit();
	}

	public void rollback()
		throws SailException
	{
		_wrappedCon.rollback();
	}

	public void addStatement(Resource subj, URI pred, Value obj, Resource context)
		throws SailException
	{
		_wrappedCon.addStatement(subj, pred, obj, context);
	}

	public void removeStatement(Resource subj, URI pred, Value obj, Resource context)
		throws SailException
	{
		_wrappedCon.removeStatement(subj, pred, obj, context);
	}

	public void clearContext(Resource context)
		throws SailException
	{
		_wrappedCon.clearContext(context);
	}

	public void clear()
		throws SailException
	{
		_wrappedCon.clear();
	}

	public TupleQueryResult evaluate(TupleQuery tupleQuery, boolean includeInferred) {
		return _wrappedCon.evaluate(tupleQuery, includeInferred);
	}

	public GraphQueryResult evaluate(GraphQuery graphQuery, boolean includeInferred) {
		return _wrappedCon.evaluate(graphQuery, includeInferred);
	}

	public CloseableIterator<? extends Resource> getContextIDs() {
		return _wrappedCon.getContextIDs();
	}

	public CloseableIterator<? extends Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean useInference)
	{
		return _wrappedCon.getStatements(subj, pred, obj, useInference);
	}

	public CloseableIterator<? extends Statement> getNullContextStatements(Resource subj, URI pred, Value obj,
			boolean useInference)
	{
		return _wrappedCon.getNullContextStatements(subj, pred, obj, useInference);
	}

	public CloseableIterator<? extends Statement> getNamedContextStatements(Resource subj, URI pred,
			Value obj, Resource context, boolean useInference)
	{
		return _wrappedCon.getNamedContextStatements(subj, pred, obj, context, useInference);
	}

	public void setNamespace(String prefix, String name)
		throws SailException
	{
		_wrappedCon.setNamespace(prefix, name);
	}

	public void removeNamespace(String prefix)
		throws SailException
	{
		_wrappedCon.removeNamespace(prefix);
	}

	public CloseableIterator<? extends Namespace> getNamespaces() {
		return _wrappedCon.getNamespaces();
	}

	public void addConnectionListener(SailConnectionListener listener) {
		_wrappedCon.addConnectionListener(listener);
	}

	public void removeConnectionListener(SailConnectionListener listener) {
		_wrappedCon.addConnectionListener(listener);
	}
}
