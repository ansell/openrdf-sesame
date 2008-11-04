/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Cursor;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.SailConnection;
import org.openrdf.store.StoreException;

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
	private SailConnection wrappedCon;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TransactionWrapper object that wraps the supplied
	 * connection.
	 */
	public SailConnectionWrapper(SailConnection wrappedCon) {
		this.wrappedCon = wrappedCon;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the connection that is wrapped by this object.
	 * 
	 * @return The SailConnection object that was supplied to the constructor of
	 *         this class.
	 */
	protected SailConnection getWrappedConnection() {
		return wrappedCon;
	}

	public boolean isOpen()
		throws StoreException
	{
		return wrappedCon.isOpen();
	}

	public void close()
		throws StoreException
	{
		wrappedCon.close();
	}

	public Cursor<? extends BindingSet> evaluate(TupleExpr tupleExpr,
			Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws StoreException
	{
		return wrappedCon.evaluate(tupleExpr, dataset, bindings, includeInferred);
	}

	public Cursor<? extends Resource> getContextIDs()
		throws StoreException
	{
		return wrappedCon.getContextIDs();
	}

	public Cursor<? extends Statement> getStatements(Resource subj, URI pred,
			Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		return wrappedCon.getStatements(subj, pred, obj, includeInferred, contexts);
	}

	public long size(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		return wrappedCon.size(subj, pred, obj, contexts);
	}

	public void begin()
		throws StoreException
	{
		wrappedCon.begin();
	}

	public void commit()
		throws StoreException
	{
		wrappedCon.commit();
	}

	public void rollback()
		throws StoreException
	{
		wrappedCon.rollback();
	}

	public void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		wrappedCon.addStatement(subj, pred, obj, contexts);
	}

	public void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		wrappedCon.removeStatements(subj, pred, obj, contexts);
	}

	public Cursor<? extends Namespace> getNamespaces()
		throws StoreException
	{
		return wrappedCon.getNamespaces();
	}

	public String getNamespace(String prefix)
		throws StoreException
	{
		return wrappedCon.getNamespace(prefix);
	}

	public void setNamespace(String prefix, String name)
		throws StoreException
	{
		wrappedCon.setNamespace(prefix, name);
	}

	public void removeNamespace(String prefix)
		throws StoreException
	{
		wrappedCon.removeNamespace(prefix);
	}

	public void clearNamespaces()
		throws StoreException
	{
		wrappedCon.clearNamespaces();
	}
}
