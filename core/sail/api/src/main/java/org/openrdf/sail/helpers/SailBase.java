/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.io.File;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.sail.Sail;
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
public abstract class SailBase implements Sail {

	/*-----------*
	 * Constants *
	 *-----------*/

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

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Directory to store information related to this sail in.
	 */
	private volatile File dataDir;

	/**
	 * Flag indicating whether the Sail is shutting down.
	 */
	private volatile boolean shutDownInProgress = false;

	/**
	 * Connection timeout on shutdown (in ms). Defaults to
	 * {@link #DEFAULT_CONNECTION_TIMEOUT}.
	 */
	protected volatile long connectionTimeOut = DEFAULT_CONNECTION_TIMEOUT;

	/**
	 * Map used to track active connections and where these were acquired. The
	 * Throwable value may be null in case debugging was disable at the time the
	 * connection was acquired.
	 */
	private final Map<SailConnection, Throwable> activeConnections = new IdentityHashMap<SailConnection, Throwable>();

	/*---------*
	 * Methods *
	 *---------*/

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
			throw new IllegalStateException("shut down in progress");
		}

		SailConnection connection = getConnectionInternal();

		Throwable stackTrace = debugEnabled() ? new Throwable() : null;
		synchronized (activeConnections) {
			activeConnections.put(connection, stackTrace);
		}

		return connection;
	}

	public String toString() {
		if (dataDir == null) {
			return super.toString();
		}
		else {
			return dataDir.toString();
		}
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
			// Check if any active connections exist. If so, wait for a grace
			// period for them to finish.
			if (!activeConnections.isEmpty()) {
				logger.debug("Waiting for active connections to close before shutting down...");
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
}
