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
package org.openrdf.sail.memory;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import info.aduna.concurrent.locks.Lock;

import org.openrdf.IsolationLevels;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.IRI;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolverClient;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolverImpl;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailChangedEvent;
import org.openrdf.sail.SailException;
import org.openrdf.sail.base.SailDataset;
import org.openrdf.sail.base.SailSink;
import org.openrdf.sail.base.SailStore;
import org.openrdf.sail.helpers.DirectoryLockManager;
import org.openrdf.sail.helpers.AbstractNotifyingSail;
import org.openrdf.sail.memory.model.MemIRI;

/**
 * An implementation of the Sail interface that stores its data in main memory
 * and that can use a file for persistent storage. This Sail implementation
 * supports single, isolated transactions. This means that changes to the data
 * are not visible until a transaction is committed and that concurrent
 * transactions are not possible. When another transaction is active, calls to
 * <tt>startTransaction()</tt> will block until the active transaction is
 * committed or rolled back.
 * 
 * @author Arjohn Kampman
 * @author jeen
 */
public class MemoryStore extends AbstractNotifyingSail implements FederatedServiceResolverClient {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected static final String DATA_FILE_NAME = "memorystore.data";

	protected static final String SYNC_FILE_NAME = "memorystore.sync";

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Factory/cache for MemValue objects.
	 */
	private SailStore store;

	private volatile boolean persist = false;

	/**
	 * The file used for data persistence, null if this is a volatile RDF store.
	 */
	private volatile File dataFile;

	/**
	 * The file used for serialising data, null if this is a volatile RDF store.
	 */
	private volatile File syncFile;

	/**
	 * The directory lock, null if this is read-only or a volatile RDF store.
	 */
	private volatile Lock dirLock;

	/**
	 * Flag indicating whether the contents of this repository have changed.
	 */
	private volatile boolean contentsChanged;

	/**
	 * The sync delay.
	 * 
	 * @see #setSyncDelay
	 */
	private volatile long syncDelay = 0L;

	/**
	 * Semaphore used to synchronize concurrent access to {@link #syncWithLock()}
	 * .
	 */
	private final Object syncSemaphore = new Object();

	/**
	 * The timer used to trigger file synchronization.
	 */
	private volatile Timer syncTimer;

	/**
	 * The currently scheduled timer task, if any.
	 */
	private volatile TimerTask syncTimerTask;

	/**
	 * Semaphore used to synchronize concurrent access to {@link #syncTimer} and
	 * {@link #syncTimerTask}.
	 */
	private final Object syncTimerSemaphore = new Object();

	/** independent life cycle */
	private FederatedServiceResolver serviceResolver;

	/** dependent life cycle */
	private FederatedServiceResolverImpl dependentServiceResolver;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new MemoryStore.
	 */
	public MemoryStore() {
		setSupportedIsolationLevels(IsolationLevels.NONE, IsolationLevels.READ_COMMITTED,
				IsolationLevels.SNAPSHOT_READ, IsolationLevels.SNAPSHOT, IsolationLevels.SERIALIZABLE);
		setDefaultIsolationLevel(IsolationLevels.SNAPSHOT_READ);
	}

	/**
	 * Creates a new persistent MemoryStore. If the specified data directory
	 * contains an existing store, its contents will be restored upon
	 * initialization.
	 * 
	 * @param dataDir
	 *        the data directory to be used for persistence.
	 */
	public MemoryStore(File dataDir) {
		this();
		setDataDir(dataDir);
		setPersist(true);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setPersist(boolean persist) {
		if (isInitialized()) {
			throw new IllegalStateException("sail has already been initialized");
		}

		this.persist = persist;
	}

	public boolean getPersist() {
		return persist;
	}

	/**
	 * Sets the time (in milliseconds) to wait after a transaction was commited
	 * before writing the changed data to file. Setting this variable to 0 will
	 * force a file sync immediately after each commit. A negative value will
	 * deactivate file synchronization until the Sail is shut down. A positive
	 * value will postpone the synchronization for at least that amount of
	 * milliseconds. If in the meantime a new transaction is started, the file
	 * synchronization will be rescheduled to wait for another <tt>syncDelay</tt>
	 * ms. This way, bursts of transaction events can be combined in one file
	 * sync.
	 * <p>
	 * The default value for this parameter is <tt>0</tt> (immediate
	 * synchronization).
	 * 
	 * @param syncDelay
	 *        The sync delay in milliseconds.
	 */
	public void setSyncDelay(long syncDelay) {
		if (isInitialized()) {
			throw new IllegalStateException("sail has already been initialized");
		}

		this.syncDelay = syncDelay;
	}

	/**
	 * Gets the currently configured sync delay.
	 * 
	 * @return syncDelay The sync delay in milliseconds.
	 * @see #setSyncDelay
	 */
	public long getSyncDelay() {
		return syncDelay;
	}

	/**
	 * @return Returns the SERVICE resolver.
	 */
	public synchronized FederatedServiceResolver getFederatedServiceResolver() {
		if (serviceResolver == null) {
			if (dependentServiceResolver == null) {
				dependentServiceResolver = new FederatedServiceResolverImpl();
			}
			return serviceResolver = dependentServiceResolver;
		}
		return serviceResolver;
	}

	/**
	 * Overrides the {@link FederatedServiceResolver} used by this instance, but
	 * the given resolver is not shutDown when this instance is.
	 * 
	 * @param reslover
	 *        The SERVICE resolver to set.
	 */
	public synchronized void setFederatedServiceResolver(FederatedServiceResolver reslover) {
		this.serviceResolver = reslover;
	}

	/**
	 * Initializes this repository. If a persistence file is defined for the
	 * store, the contents will be restored.
	 * 
	 * @throws SailException
	 *         when initialization of the store failed.
	 */
	protected void initializeInternal()
		throws SailException
	{
		logger.debug("Initializing MemoryStore...");

		this.store = new MemorySailStore(debugEnabled());

		if (persist) {
			File dataDir = getDataDir();
			DirectoryLockManager locker = new DirectoryLockManager(dataDir);
			dataFile = new File(dataDir, DATA_FILE_NAME);
			syncFile = new File(dataDir, SYNC_FILE_NAME);

			if (dataFile.exists()) {
				logger.debug("Reading data from {}...", dataFile);

				// Initialize persistent store from file
				if (!dataFile.canRead()) {
					logger.error("Data file is not readable: {}", dataFile);
					throw new SailException("Can't read data file: " + dataFile);
				}
				// try to create a lock for later writing
				dirLock = locker.tryLock();
				if (dirLock == null) {
					logger.warn("Failed to lock directory: {}", dataDir);
				}
				// Don't try to read empty files: this will result in an
				// IOException, and the file doesn't contain any data anyway.
				if (dataFile.length() == 0L) {
					logger.warn("Ignoring empty data file: {}", dataFile);
				}
				else {
					SailSink explicit = store.getExplicitSailSource().sink(IsolationLevels.NONE);
					SailSink inferred = store.getInferredSailSource().sink(IsolationLevels.NONE);
					try {
						new FileIO(store.getValueFactory()).read(dataFile, explicit, inferred);
						logger.debug("Data file read successfully");
					}
					catch (IOException e) {
						logger.error("Failed to read data file", e);
						throw new SailException(e);
					}
					finally {
						explicit.prepare();
						explicit.flush();
						explicit.close();
						inferred.prepare();
						inferred.flush();
						inferred.close();
					}
				}
			}
			else {
				// file specified that does not exist yet, create it
				try {
					File dir = dataFile.getParentFile();
					if (dir != null && !dir.exists()) {
						logger.debug("Creating directory for data file...");
						if (!dir.mkdirs()) {
							logger.debug("Failed to create directory for data file: {}", dir);
							throw new SailException("Failed to create directory for data file: " + dir);
						}
					}
					// try to lock directory or fail
					dirLock = locker.lockOrFail();

					logger.debug("Initializing data file...");
					SailDataset explicit = store.getExplicitSailSource().dataset(IsolationLevels.SNAPSHOT);
					SailDataset inferred = store.getInferredSailSource().dataset(IsolationLevels.SNAPSHOT);
					try {
						new FileIO(store.getValueFactory()).write(explicit, inferred, syncFile, dataFile);
					}
					finally {
						explicit.close();
						inferred.close();

					}
					logger.debug("Data file initialized");
				}
				catch (IOException e) {
					logger.debug("Failed to initialize data file", e);
					throw new SailException("Failed to initialize data file " + dataFile, e);
				}
				catch (SailException e) {
					logger.debug("Failed to initialize data file", e);
					throw new SailException("Failed to initialize data file " + dataFile, e);
				}
			}
		}

		contentsChanged = false;

		logger.debug("MemoryStore initialized");
	}

	@Override
	protected void shutDownInternal()
		throws SailException
	{
		try {
			cancelSyncTimer();
			sync();

			store.close();
			dataFile = null;
			syncFile = null;
		}
		finally {
			if (dirLock != null) {
				dirLock.release();
			}
			if (dependentServiceResolver != null) {
				dependentServiceResolver.shutDown();
			}
		}
	}

	/**
	 * Checks whether this Sail object is writable. A MemoryStore is not writable
	 * if a read-only data file is used.
	 */
	public boolean isWritable() {
		// Sail is not writable when it has a dataDir but no directory lock
		return !persist || dirLock != null;
	}

	@Override
	protected NotifyingSailConnection getConnectionInternal()
		throws SailException
	{
		return new MemoryStoreConnection(this);
	}

	public ValueFactory getValueFactory() {
		if (store == null) {
			throw new IllegalStateException("sail not initialized.");
		}

		return store.getValueFactory();
	}

	@Override
	public void notifySailChanged(SailChangedEvent event) {
		super.notifySailChanged(event);
		contentsChanged = true;
	}

	protected void scheduleSyncTask()
		throws SailException
	{
		if (!persist) {
			return;
		}

		if (syncDelay == 0L) {
			// Sync immediately
			sync();
		}
		else if (syncDelay > 0L) {
			synchronized (syncTimerSemaphore) {
				// Sync in syncDelay milliseconds
				if (syncTimer == null) {
					// Create the syncTimer on a deamon thread
					syncTimer = new Timer("MemoryStore synchronization", true);
				}

				if (syncTimerTask != null) {
					logger.error("syncTimerTask is not null");
				}

				syncTimerTask = new TimerTask() {

					@Override
					public void run() {
						try {
							sync();
						}
						catch (SailException e) {
							logger.warn("Unable to sync on timer", e);
						}
					}
				};

				syncTimer.schedule(syncTimerTask, syncDelay);
			}
		}
	}

	protected void cancelSyncTask() {
		synchronized (syncTimerSemaphore) {
			if (syncTimerTask != null) {
				syncTimerTask.cancel();
				syncTimerTask = null;
			}
		}
	}

	protected void cancelSyncTimer() {
		synchronized (syncTimerSemaphore) {
			if (syncTimer != null) {
				syncTimer.cancel();
				syncTimer = null;
			}
		}
	}

	/**
	 * Synchronizes the contents of this repository with the data that is stored
	 * on disk. Data will only be written when the contents of the repository and
	 * data in the file are out of sync.
	 */
	public void sync()
		throws SailException
	{
		// syncSemaphore prevents concurrent file synchronizations
		synchronized (syncSemaphore) {
			if (persist && contentsChanged) {
				logger.debug("syncing data to file...");
				try {
					IsolationLevels level = IsolationLevels.SNAPSHOT;
					SailDataset explicit = store.getExplicitSailSource().dataset(level);
					SailDataset inferred = store.getInferredSailSource().dataset(level);
					try {
						new FileIO(store.getValueFactory()).write(explicit, inferred, syncFile, dataFile);
					}
					finally {
						explicit.close();
						inferred.close();
					}
					contentsChanged = false;
					logger.debug("Data synced to file");
				}
				catch (IOException e) {
					logger.error("Failed to sync to file", e);
					throw new SailException(e);
				}
			}
		}
	}

	SailStore getSailStore() {
		return store;
	}
}
