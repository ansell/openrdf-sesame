/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract Sail implementation that takes care of common sail tasks,
 * including proper closing of active connections and a grace period for active
 * connections during shutdown of the store.
 * 
 * @author Herko ter Horst
 * @author jeen
 * @author Arjohn Kampman
 */
public abstract class AbstractSail implements Sail {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * Default connection timeout on shutdown: 20,000 milliseconds.
	 */
	protected final static long DEFAULT_CONNECTION_TIMEOUT = 20000L;

	/**
	 * default transaction isolation level, set to
	 * {@link IsolationLevels#READ_COMMITTED }.
	 */
	private IsolationLevel defaultIsolationLevel = IsolationLevels.READ_COMMITTED;

	/**
	 * list of supported isolation levels. By default set to include
	 * {@link IsolationLevels#READ_UNCOMMITTED} and
	 * {@link IsolationLevels#SERIALIZABLE}. Specific store implementations are
	 * expected to alter this list according to their specific capabilities.
	 */
	private List<IsolationLevel> supportedIsolationLevels = new ArrayList<IsolationLevel>();

	/**
	 * default value for the Iteration item sync threshold
	 */
	protected static final long DEFAULT_ITERATION_SYNC_THRESHOLD = 0L;

	// Note: the following variable and method are package protected so that they
	// can be removed when open connections no longer block other connections and
	// they can be closed silently (just like in JDBC).
	static final String DEBUG_PROP = "org.eclipse.rdf4j.repository.debug";

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
	 * Directory to store information related to this sail in (if any).
	 */
	private volatile File dataDir;

	/**
	 * Flag indicating whether the Sail has been initialized. Sails are
	 * initialized from {@link #initialize() initialization} until
	 * {@link #shutDown() shutdown}.
	 */
	private volatile boolean initialized = false;

	/**
	 * Lock used to synchronize the initialization state of a sail.
	 * <ul>
	 * <li>write lock: initialize(), shutDown()
	 * <li>read lock: getConnection()
	 * </ul>
	 */
	protected final ReentrantReadWriteLock initializationLock = new ReentrantReadWriteLock();

	/**
	 * Connection timeout on shutdown (in ms). Defaults to
	 * {@link #DEFAULT_CONNECTION_TIMEOUT}.
	 */
	protected volatile long connectionTimeOut = DEFAULT_CONNECTION_TIMEOUT;

	private long iterationCacheSyncThreshold = DEFAULT_ITERATION_SYNC_THRESHOLD;

	/**
	 * Map used to track active connections and where these were acquired. The
	 * Throwable value may be null in case debugging was disable at the time the
	 * connection was acquired.
	 */
	private final Map<SailConnection, Throwable> activeConnections = new IdentityHashMap<SailConnection, Throwable>();

	/*
	 * constructors
	 */

	public AbstractSail() {
		super();
		this.addSupportedIsolationLevel(IsolationLevels.READ_UNCOMMITTED);
		this.addSupportedIsolationLevel(IsolationLevels.SERIALIZABLE);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void setDataDir(File dataDir) {
		if (isInitialized()) {
			throw new IllegalStateException("sail has already been initialized");
		}

		this.dataDir = dataDir;
	}

	@Override
	public File getDataDir() {
		return dataDir;
	}

	@Override
	public String toString() {
		if (dataDir == null) {
			return super.toString();
		}
		else {
			return dataDir.toString();
		}
	}

	/**
	 * Checks whether the Sail has been initialized. Sails are initialized from
	 * {@link #initialize() initialization} until {@link #shutDown() shutdown}.
	 * 
	 * @return <tt>true</tt> if the Sail has been initialized, <tt>false</tt>
	 *         otherwise.
	 */
	protected boolean isInitialized() {
		return initialized;
	}

	@Override
	public void initialize()
		throws SailException
	{
		initializationLock.writeLock().lock();
		try {
			if (isInitialized()) {
				throw new IllegalStateException(
						"Sail has already been intialized. Ensure this Sail is being used via a Repository.");
			}

			initializeInternal();

			initialized = true;
		}
		finally {
			initializationLock.writeLock().unlock();
		}
	}

	/**
	 * Do store-specific operations to initialize the store. The default
	 * implementation of this method does nothing.
	 */
	protected void initializeInternal()
		throws SailException
	{
	}

	@Override
	public void shutDown()
		throws SailException
	{
		initializationLock.writeLock().lock();
		try {
			if (!isInitialized()) {
				return;
			}

			Map<SailConnection, Throwable> activeConnectionsCopy;

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

				// Copy the current contents of the map so that we don't have to
				// synchronize on activeConnections. This prevents a potential
				// deadlock with concurrent calls to connectionClosed()
				activeConnectionsCopy = new IdentityHashMap<SailConnection, Throwable>(activeConnections);
			}

			// Forcefully close any connections that are still open
			for (Map.Entry<SailConnection, Throwable> entry : activeConnectionsCopy.entrySet()) {
				SailConnection con = entry.getKey();
				Throwable stackTrace = entry.getValue();

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

			// All connections should be closed now
			synchronized (activeConnections) {
				activeConnections.clear();
			}

			shutDownInternal();
		}
		finally {
			initialized = false;
			initializationLock.writeLock().unlock();
		}
	}

	/**
	 * Do store-specific operations to ensure proper shutdown of the store.
	 */
	protected abstract void shutDownInternal()
		throws SailException;

	@Override
	public SailConnection getConnection()
		throws SailException
	{
		initializationLock.readLock().lock();
		try {
			if (!isInitialized()) {
				throw new IllegalStateException("Sail is not initialized or has been shut down");
			}

			SailConnection connection = getConnectionInternal();

			Throwable stackTrace = debugEnabled() ? new Throwable() : null;
			synchronized (activeConnections) {
				activeConnections.put(connection, stackTrace);
			}

			return connection;
		}
		finally {
			initializationLock.readLock().unlock();
		}
	}

	/**
	 * Returns a store-specific SailConnection object.
	 * 
	 * @return A connection to the store.
	 */
	protected abstract SailConnection getConnectionInternal()
		throws SailException;

	/**
	 * Signals to the store that the supplied connection has been closed; called
	 * by {@link AbstractSailConnection#close()}.
	 * 
	 * @param connection
	 *        The connection that has been closed.
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

	/**
	 * Appends the provided {@link IsolationLevels} to the SAIL's list of
	 * supported isolation levels.
	 * 
	 * @param level
	 *        a supported IsolationLevel.
	 * @since 2.8
	 */
	protected void addSupportedIsolationLevel(IsolationLevels level) {
		this.supportedIsolationLevels.add(level);
	}

	/**
	 * Removes all occurrences of the provided {@link IsolationLevels} in the
	 * list of supported Isolation levels.
	 * 
	 * @param level
	 *        the isolation level to remove.
	 * @since 2.8
	 */
	protected void removeSupportedIsolationLevel(IsolationLevel level) {
		while (this.supportedIsolationLevels.remove(level)) {
		}
	}

	/**
	 * Sets the list of supported {@link IsolationLevels}s for this SAIL. The
	 * list is expected to be ordered in increasing complexity.
	 * 
	 * @param supportedIsolationLevels
	 *        a list of supported isolation levels.
	 * @since 2.8
	 */
	protected void setSupportedIsolationLevels(List<IsolationLevel> supportedIsolationLevels) {
		this.supportedIsolationLevels = supportedIsolationLevels;
	}

	/**
	 * Sets the list of supported {@link IsolationLevels}s for this SAIL. The
	 * list is expected to be ordered in increasing complexity.
	 * 
	 * @param supportedIsolationLevels
	 *        a list of supported isolation levels.
	 * @since 2.8.3
	 */
	protected void setSupportedIsolationLevels(IsolationLevel... supportedIsolationLevels) {
		this.supportedIsolationLevels = Arrays.asList(supportedIsolationLevels);
	}

	@Override
	public List<IsolationLevel> getSupportedIsolationLevels() {
		return Collections.unmodifiableList(supportedIsolationLevels);
	}

	@Override
	public IsolationLevel getDefaultIsolationLevel() {
		return defaultIsolationLevel;
	}

	/**
	 * Sets the default {@link IsolationLevel} on which transactions in this Sail
	 * operate.
	 * 
	 * @since 2.8.0
	 * @param defaultIsolationLevel
	 *        The defaultIsolationLevel to set.
	 */
	public void setDefaultIsolationLevel(IsolationLevel defaultIsolationLevel) {
		if (defaultIsolationLevel == null) {
			throw new IllegalArgumentException("default isolation level may not be null");
		}
		this.defaultIsolationLevel = defaultIsolationLevel;
	}

	/**
	 * Retrieves the currently configured threshold for syncing query evaluation
	 * iteration caches to disk.
	 * 
	 * @return Returns the iterationCacheSyncThreshold.
	 */
	public long getIterationCacheSyncThreshold() {
		return iterationCacheSyncThreshold;
	}

	/**
	 * Set the threshold for syncing query evaluation iteration caches to disk.
	 * 
	 * @param iterationCacheSyncThreshold
	 *        The iterationCacheSyncThreshold to set.
	 */
	public void setIterationCacheSyncThreshold(long iterationCacheSyncThreshold) {
		this.iterationCacheSyncThreshold = iterationCacheSyncThreshold;
	}
}
