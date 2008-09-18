/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.StoreException;
import org.openrdf.model.Resource;
import org.openrdf.sail.SailConnection;

/**
 * Abstract Class offering base functionality for SailConnection
 * implementations.
 * 
 * @author Arjohn Kampman
 * @author jeen
 * @author James Leigh
 */
public abstract class SailConnectionBase implements SailConnection {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean txnActive;

	/*---------*
	 * Methods *
	 *---------*/

	public boolean isOpen()
		throws StoreException
	{
		return true;
	}

	public void close()
		throws StoreException
	{
		// no-op
	}

	protected boolean transactionActive() {
		return txnActive;
	}

	public void begin()
		throws StoreException
	{
		txnActive = true;
	}

	public void commit()
		throws StoreException
	{
		txnActive = false;
	}

	public void rollback()
		throws StoreException
	{
		txnActive = false;
	}

	public void clear(Resource... contexts)
		throws StoreException
	{
		removeStatements(null, null, null, contexts);
	}
}
