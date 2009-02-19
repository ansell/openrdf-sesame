/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.sail.SailConnection;
import org.openrdf.store.Isolation;
import org.openrdf.store.StoreException;

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

	private volatile Isolation isolation;

	private volatile boolean readOnly;

	private volatile boolean autoCommit = true;

	private volatile boolean closed;

	/*---------*
	 * Methods *
	 *---------*/

	public boolean isOpen()
		throws StoreException
	{
		return !closed;
	}

	public void close()
		throws StoreException
	{
		closed = true;
	}

	public Isolation getTransactionIsolation()
		throws StoreException
	{
		return isolation;
	}

	public void setTransactionIsolation(Isolation isolation)
		throws StoreException
	{
		this.isolation = isolation;
	}

	public boolean isReadOnly()
		throws StoreException
	{
		return readOnly;
	}

	public void setReadOnly(boolean readOnly)
		throws StoreException
	{
		this.readOnly = readOnly;
	}

	public boolean isAutoCommit() {
		return autoCommit;
	}

	public void begin()
		throws StoreException
	{
		autoCommit = false;
	}

	public void commit()
		throws StoreException
	{
		autoCommit = true;
	}

	public void rollback()
		throws StoreException
	{
		autoCommit = true;
	}
}
