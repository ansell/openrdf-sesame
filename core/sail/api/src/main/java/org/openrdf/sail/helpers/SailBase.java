/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.io.File;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.NotifyingSailConnection;
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
 * @author Arjohn Kampman
 */
public abstract class SailBase implements NotifyingSail {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Default connection timeout on shutdown: 20,000 milliseconds.
	 */
	protected final static long DEFAULT_CONNECTION_TIMEOUT = 20000L;

	// Note: the following variable and method are package protected so that they
	// can be removed when open connections no longer block other connections and
	// they can be closed silently (just like in JDBC).
	static final String DEBUG_PROP = "org.openrdf.repository.debug";

	protected static boolean debugEnabled() {
		try {
			String value = System.getProperty(DEBUG_PROP);
			return value != null && !value.equals("false");
		}
		catch (SecurityException e) {
			// Thrown when not allowed to read system properties, for example when
			// running in applets
			return false;
		}
	}

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

	/**
	 * Connection timeout on shutdown (in ms). Defaults to
	 * {@link #DEFAULT_CONNECTION_TIMEOUT}.
	 */
	protected long connectionTimeOut = DEFAULT_CONNECTION_TIMEOUT;

	/**
	 * Map used to track active connections and where these were acquired. The
	 * Throwable value may be null in case debugging was disable at the time the
	 * connection was acquired.
	 */
	private Map<SailConnection, Throwable> activeConnections = new IdentityHashMap<SailConnection, Throwable>();

	/**
	 * Objects that should be notified of changes to the data in this Sail.
	 */
	private Set<SailChangedListener> sailChangedListeners = new HashSet<SailChangedListener>(0);

	/*---------*
	 * Methods *
	 *---------*/

	public void setDataDir(File dataDir) {
		this.dataDir = dataDir;
	}

	public File getDataDir() {
		return dataDir;
	}

	public NotifyingSailConnection getConnection()
		throws SailException
	{
		if (shutDownInProgress) {
			throw new IllegalStateException("shut down in progress");
		}

		NotifyingSailConnection connection = getConnectionInternal();

		Throwable stackTrace = debugEnabled() ? new Throwable() : null;
		activeConnections.put(connection, stackTrace);

		return connection;
	}

	/**
	 * returns a store-specific SailConnection object.
	 * 
	 * @return a SailConnection
	 * @throws SailException
	 */
	protected abstract NotifyingSailConnection getConnectionInternal()
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
				logger.info("Waiting for active connections to close before shutting down...");
				try {
					activeConnections.wait(DEFAULT_CONNECTION_TIMEOUT);
				}
				catch (InterruptedException e) {
					// ignore and continue
				}
			}

			// Forcefully close any connections that are still open
			Iterator<Map.Entry<SailConnection, Throwable>> iter = activeConnections.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<SailConnection, Throwable> entry = iter.next();
				SailConnection con = entry.getKey();
				Throwable stackTrace = entry.getValue();

				iter.remove();

				if (stackTrace == null) {
					logger.warn(
							"Closing active connection due to shut down; consider setting the {} system property",
							DEBUG_PROP);
				}
				else {
					logger.warn("Closing active connection due to shut down, connection was acquired in",
							stackTrace);
				}

				try {
					con.close();
				}
				catch (SailException e) {
					logger.error("Failed to close connection", e);
				}
			}
		}

		try {
			shutDownInternal();
		}
		finally {
			shutDownInProgress = false;
		}
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
			if (activeConnections.containsKey(connection)) {
				activeConnections.remove(connection);

				if (activeConnections.isEmpty()) {
					// only notify waiting threads if all active connections have
					// been closed.
					activeConnections.notifyAll();
				}
			}
			else {
				logger.warn("tried to remove unknown connection object from store.");
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
