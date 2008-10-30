/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailMetaData;
import org.openrdf.store.StoreException;

/**
 * SailBase is an abstract Sail implementation that takes care of common sail
 * tasks, including proper closing of active connections and a grace period for
 * active connections during shutdown of the store.
 * 
 * @author Herko ter Horst
 * @author jeen
 * @author Arjohn Kampman
 * @author James Leigh
 */
public abstract class SailBase implements Sail {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Directory to store information related to this sail in.
	 */
	private File dataDir;

	/**
	 * Flag indicating whether the Sail is shutting down.
	 */
	private boolean shutDownInProgress = false;

	private final SailConnectionTracker tracker;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SailBase() {
		tracker = createSailConnectionTracker();
	}

	/*---------*
	 * Methods *
	 *---------*/

	protected SailConnectionTracker createSailConnectionTracker() {
		return new SailConnectionTracker();
	}

	public SailMetaData getSailMetaData() {
		return new SailMetaDataImpl();
	}

	public void setDataDir(File dataDir) {
		this.dataDir = dataDir;
	}

	public File getDataDir() {
		return dataDir;
	}

	public SailConnection getConnection()
		throws StoreException
	{
		if (shutDownInProgress) {
			throw new IllegalStateException("shut down in progress");
		}

		return tracker.track(getConnectionInternal());
	}

	/**
	 * returns a store-specific SailConnection object.
	 * 
	 * @return a SailConnection
	 * @throws StoreException
	 */
	protected abstract SailConnection getConnectionInternal()
		throws StoreException;

	public void shutDown()
		throws StoreException
	{
		// indicate no more new connections should be given out.
		shutDownInProgress = true;

		try {
			tracker.closeAll();

			shutDownInternal();
		}
		finally {
			shutDownInProgress = false;
		}
	}

	/**
	 * Do store-specific operations to ensure proper shutdown of the store.
	 * 
	 * @throws StoreException
	 */
	protected abstract void shutDownInternal()
		throws StoreException;
}
