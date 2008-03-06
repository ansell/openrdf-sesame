/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailChangedEvent;
import org.openrdf.sail.SailChangedListener;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * SailBase is an abstract Sail implementation that takes care of common sail
 * tasks, including proper closing of active connections and a grace period for
 * active connections during shutdown of the store.
 * 
 * @author Herko ter Horst
 * @author jeen
 */
public abstract class SailBase implements Sail {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	/** connection timeout on shutdown (in ms), default is 20,000 ms. */
	public static long CONNECTION_TIMEOUT = 20000L;

	/**
	 * Directory to store information related to this sail in.
	 */
	private File dataDir = null;

	/**
	 * Collection of active connections to be closed when the store shuts down.
	 */
	private Collection<SailConnection> activeConnections = new LinkedHashSet<SailConnection>(1);

	private boolean shutDownInProgress = false;

	/**
	 * Objects that should be notified of changes to the data in this Sail.
	 */
	private Set<SailChangedListener> sailChangedListeners = new LinkedHashSet<SailChangedListener>(0);

	public void setParameter(String key, String value) {
		if (DATA_DIR_KEY.equals(key)) {
			setDataDir(new File(value));
		}
	}

	public void setDataDir(File dataDir) {
		this.dataDir = dataDir;
	}

	public File getDataDir() {
		return dataDir;
	}

	public SailConnection getConnection()
		throws SailException
	{
		if (shutDownInProgress) {
			throw new IllegalStateException("shutdown in progress.");
		}
		SailConnection connection = getConnectionInternal();
		activeConnections.add(connection);
		return connection;
	}

	/**
	 * returns a store-specific SailConnection object.
	 * 
	 * @return a SailConnection
	 * @throws SailException
	 */
	protected abstract SailConnection getConnectionInternal()
		throws SailException;

	public void shutDown()
		throws SailException
	{
		// indicate no more new connections should be given out.
		shutDownInProgress = true;
		synchronized (activeConnections) {

			// check if any active connections exist, if so, wait for a grace
			// period for them to finish.
			if (!activeConnections.isEmpty()) {
				logger.warn("Waiting for active connections to finish...");
				try {
					activeConnections.wait(CONNECTION_TIMEOUT);
				}
				catch (InterruptedException e) {
					// ignore and continue
				}
			}

			// if any connections still active after grace period, force them
			// closed.
			for (SailConnection con : activeConnections) {
				logger.warn("Forcing active connection closed...");
				con.close();
			}
			assert activeConnections.isEmpty();
		} // end synchronized block

		shutDownInternal();
	}

	/**
	 * Do store-specific operations to ensure proper shutdown of the store.
	 * 
	 * @throws SailException
	 */
	protected abstract void shutDownInternal()
		throws SailException;

	/**
	 * Signals to the store that the supplied connection has been closed.
	 * 
	 * @param connection
	 */
	protected void connectionClosed(SailConnection connection) {
		synchronized (activeConnections) {
			boolean removed = activeConnections.remove(connection);
			if (!removed) {
				logger.warn("trying to remove unknown connection object from store.");
			}
			else {
				if (activeConnections.isEmpty()) {
					// only notify waiting threads if all active connections have
					// been closed.
					activeConnections.notifyAll();
				}
			}
		}
	}

	public void addSailChangedListener(SailChangedListener listener) {
		synchronized (sailChangedListeners) {
			sailChangedListeners.add(listener);
		}
	}

	public void removeSailChangedListener(SailChangedListener listener) {
		synchronized (sailChangedListeners) {
			sailChangedListeners.remove(listener);
		}
	}

	/**
	 * Notifies all registered SailChangedListener's of changes to the contents
	 * of this Sail.
	 */
	public void notifySailChanged(SailChangedEvent event) {
		synchronized (sailChangedListeners) {
			for (SailChangedListener l : sailChangedListeners) {
				l.sailChanged(event);
			}
		}
	}
}
