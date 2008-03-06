/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.inferencer;

import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionWrapper;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;


/**
 * An extension of ConnectionWrapper that implements the InferencerConnection
 * interface. 
 */
public class InferencerConnectionWrapper
	extends SailConnectionWrapper
	implements InferencerConnection
{

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new InferencerTransactionWrapper object that wraps the supplied
	 * transaction.
	 */
	public InferencerConnectionWrapper(InferencerConnection wrappedTxn) {
		super(wrappedTxn);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the transaction that is wrapped by this object.
	 *
	 * @return The InferencerTransaction object that was supplied to the
	 * constructor of this class.
	 */
	protected InferencerConnection getWrappedInferencerTransaction() {
		return (InferencerConnection)getWrappedConnection();
	}
	
	// Implements InferencerTransaction.addInferredStatement(...)
	public boolean addInferredStatement(
		Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		return getWrappedInferencerTransaction().addInferredStatement(
			subj, pred, obj, contexts);
	}
	
	// Implements InferencerTransaction.removeInferredStatement(...)
	public boolean removeInferredStatement(
		Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		return getWrappedInferencerTransaction().removeInferredStatement(
			subj, pred, obj, contexts);
	}

	// Implements InferencerTransaction.clearInferred()
	public void clearInferred()
		throws SailException
	{
		getWrappedInferencerTransaction().clearInferred();
	}

	// Implements InferencerTransaction.clearInferredFromContext(...)
	public void clearInferredFromContext(Resource... contexts)
		throws SailException
	{
		getWrappedInferencerTransaction().clearInferredFromContext(contexts);
	}
}
