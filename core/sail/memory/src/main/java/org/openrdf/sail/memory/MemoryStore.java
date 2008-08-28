/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory;

import java.io.File;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import info.aduna.concurrent.locks.ExclusiveLockManager;
import info.aduna.concurrent.locks.Lock;
import info.aduna.concurrent.locks.ReadPrefReadWriteLockManager;
import info.aduna.concurrent.locks.ReadWriteLockManager;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.StoreException;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.helpers.DirectoryLockManager;
import org.openrdf.sail.helpers.NotifyingSailBase;
import org.openrdf.sail.helpers.SynchronizedSailConnection;
import org.openrdf.sail.memory.model.MemResource;
import org.openrdf.sail.memory.model.MemStatement;
import org.openrdf.sail.memory.model.MemStatementIterator;
import org.openrdf.sail.memory.model.MemStatementList;
import org.openrdf.sail.memory.model.MemURI;
import org.openrdf.sail.memory.model.MemValue;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.openrdf.sail.memory.model.ReadMode;
import org.openrdf.sail.memory.model.TxnStatus;

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
public class MemoryStore extends NotifyingSailBase {

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
	private MemValueFactory valueFactory;

	/**
	 * List containing all available statements.
	 */
	private MemStatementList statements;

	/**
	 * Set of all statements that have been affected by a transaction.
	 */
	private IdentityHashMap<MemStatement, MemStatement> txnStatements;

	/**
	 * Identifies the current snapshot.
	 */
	private int currentSnapshot;

	/**
	 * Store for namespace prefix info.
	 */
	private MemNamespaceStore namespaceStore;

	/**
	 * Lock manager used to give the snapshot cleanup thread exclusive access to
	 * the statement list.
	 */
	private ReadWriteLockManager statementListLockManager;

	/**
	 * Lock manager used to prevent concurrent transactions.
	 */
	private ExclusiveLockManager txnLockManager;

	/**
	 * Flag indicating whether the Sail has been initialized.
	 */
	private boolean initialized = false;

	private boolean persist = false;

	/**
	 * The file used for data persistence, null if this is a volatile RDF store.
	 */
	private File dataFile;

	/**
	 * The file used for serialising data, null if this is a volatile RDF store.
	 */
	private File syncFile;

	/**
	 * The directory lock, null if this is read-only or a volatile RDF store.
	 */
	private Lock dirLock;

	/**
	 * Flag indicating whether the contents of this repository have changed.
	 */
	private boolean contentsChanged;

	/**
	 * The sync delay.
	 * 
	 * @see #setSyncDelay
	 */
	private long syncDelay = 0L;

	/**
	 * Semaphore used to synchronize concurrent access to {@link #sync()}.
	 */
	private final Object syncSemaphore = new Object();

	/**
	 * The timer used to trigger file synchronization.
	 */
	private Timer syncTimer;

	/**
	 * The currently scheduled timer task, if any.
	 */
	private TimerTask syncTimerTask;

	/**
	 * Semaphore used to synchronize concurrent access to {@link #syncTimer} and
	 * {@link #syncTimerTask}.
	 */
	private final Object syncTimerSemaphore = new Object();

	/**
	 * Cleanup thread that removes deprecated statements when no other threads
	 * are accessing this list. Seee {@link #scheduleSnapshotCleanup()}.
	 */
	private Thread snapshotCleanupThread;

	/**
	 * Semaphore used to synchronize concurrent access to
	 * {@link #snapshotCleanupThread}.
	 */
	private final Object snapshotCleanupThreadSemaphore = new Object();

	private boolean trackLocks = false;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new MemoryStore.
	 */
	public MemoryStore() {
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
		setDataDir(dataDir);
		setPersist(true);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void setDataDir(File dataDir) {
		if (isInitialized()) {
			throw new IllegalStateException("sail has already been initialized");
		}

		super.setDataDir(dataDir);
	}

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
	 * Initializes this repository. If a persistence file is defined for the
	 * store, the contents will be restored.
	 * 
	 * @throws StoreException
	 *         when initialization of the store failed.
	 */
	public void initialize()
		throws StoreException
	{
		if (isInitialized()) {
			throw new IllegalStateException("sail has already been intialized");
		}

		logger.debug("Initializing MemoryStore...");

		statementListLockManager = new ReadPrefReadWriteLockManager(trackLocks);
		txnLockManager = new ExclusiveLockManager(trackLocks);
		namespaceStore = new MemNamespaceStore();

		valueFactory = new MemValueFactory();
		statements = new MemStatementList(256);
		currentSnapshot = 1;

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
					throw new StoreException("Can't read data file: " + dataFile);
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
					try {
						FileIO.read(this, dataFile);
						logger.debug("Data file read successfully");
					}
					catch (IOException e) {
						logger.error("Failed to read data file", e);
						throw new StoreException(e);
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
							throw new StoreException("Failed to create directory for data file: " + dir);
						}
					}
					// try to lock directory or fail
					dirLock = locker.lockOrFail();

					logger.debug("Initializing data file...");
					FileIO.write(this, syncFile, dataFile);
					logger.debug("Data file initialized");
				}
				catch (IOException e) {
					logger.debug("Failed to initialize data file", e);
					throw new StoreException("Failed to initialize data file " + dataFile, e);
				}
			}
		}

		contentsChanged = false;
		initialized = true;

		logger.debug("MemoryStore initialized");
	}

	/**
	 * Checks whether the Sail has been initialized.
	 * 
	 * @return <tt>true</tt> if the Sail has been initialized, <tt>false</tt>
	 *         otherwise.
	 */
	protected final boolean isInitialized() {
		return initialized;
	}

	@Override
	protected void shutDownInternal()
		throws StoreException
	{
		if (isInitialized()) {
			Lock stLock = getStatementsReadLock();

			try {
				cancelSyncTimer();
				sync();

				valueFactory = null;
				statements = null;
				dataFile = null;
				syncFile = null;
				initialized = false;
			}
			finally {
				stLock.release();
				if (dirLock != null) {
					dirLock.release();
				}
			}
		}
	}

	/**
	 * Checks whether this Sail object is writable. A MemoryStore is not writable
	 * if a read-only data file is used.
	 */
	public boolean isWritable() {
		// Sail is not writable when it has a dataDir, but no directory lock
		return !persist || dirLock != null;
	}

	@Override
	protected NotifyingSailConnection getConnectionInternal()
		throws StoreException
	{
		if (!isInitialized()) {
			throw new IllegalStateException("sail not initialized.");
		}

		NotifyingSailConnection con = new MemoryStoreConnection(this);
		return new SynchronizedSailConnection(con);
	}

	public MemValueFactory getValueFactory() {
		if (valueFactory == null) {
			throw new IllegalStateException("sail not initialized.");
		}

		return valueFactory;
	}

	protected MemNamespaceStore getNamespaceStore() {
		return namespaceStore;
	}

	protected MemStatementList getStatements() {
		return statements;
	}

	protected int getCurrentSnapshot() {
		return currentSnapshot;
	}

	protected Lock getStatementsReadLock()
		throws StoreException
	{
		try {
			return statementListLockManager.getReadLock();
		}
		catch (InterruptedException e) {
			throw new StoreException(e);
		}
	}

	protected Lock getTransactionLock()
		throws StoreException
	{
		try {
			return txnLockManager.getExclusiveLock();
		}
		catch (InterruptedException e) {
			throw new StoreException(e);
		}
	}

	protected int size() {
		return statements.size();
	}

	/**
	 * Creates a StatementIterator that contains the statements matching the
	 * specified pattern of subject, predicate, object, context. Inferred
	 * statements are excluded when <tt>explicitOnly</tt> is set to
	 * <tt>true</tt>. Statements from the null context are excluded when
	 * <tt>namedContextsOnly</tt> is set to <tt>true</tt>. The returned
	 * StatementIterator will assume the specified read mode.
	 */
	protected <X extends Exception> CloseableIteration<MemStatement, X> createStatementIterator(
			Class<X> excClass, Resource subj, URI pred, Value obj, boolean explicitOnly, int snapshot,
			ReadMode readMode, Resource... contexts)
	{
		// Perform look-ups for value-equivalents of the specified values
		MemResource memSubj = valueFactory.getMemResource(subj);
		if (subj != null && memSubj == null) {
			// non-existent subject
			return new EmptyIteration<MemStatement, X>();
		}

		MemURI memPred = valueFactory.getMemURI(pred);
		if (pred != null && memPred == null) {
			// non-existent predicate
			return new EmptyIteration<MemStatement, X>();
		}

		MemValue memObj = valueFactory.getMemValue(obj);
		if (obj != null && memObj == null) {
			// non-existent object
			return new EmptyIteration<MemStatement, X>();
		}

		MemResource[] memContexts;
		MemStatementList smallestList;

		if (contexts.length == 0) {
			memContexts = new MemResource[0];
			smallestList = statements;
		}
		else if (contexts.length == 1 && contexts[0] != null) {
			MemResource memContext = valueFactory.getMemResource(contexts[0]);
			if (memContext == null) {
				// non-existent context
				return new EmptyIteration<MemStatement, X>();
			}

			memContexts = new MemResource[] { memContext };
			smallestList = memContext.getContextStatementList();
		}
		else {
			Set<MemResource> contextSet = new LinkedHashSet<MemResource>(2 * contexts.length);

			for (Resource context : contexts) {
				MemResource memContext = valueFactory.getMemResource(context);
				if (context == null || memContext != null) {
					contextSet.add(memContext);
				}
			}

			if (contextSet.isEmpty()) {
				// no known contexts specified
				return new EmptyIteration<MemStatement, X>();
			}

			memContexts = contextSet.toArray(new MemResource[contextSet.size()]);
			smallestList = statements;
		}

		if (memSubj != null) {
			MemStatementList l = memSubj.getSubjectStatementList();
			if (l.size() < smallestList.size()) {
				smallestList = l;
			}
		}

		if (memPred != null) {
			MemStatementList l = memPred.getPredicateStatementList();
			if (l.size() < smallestList.size()) {
				smallestList = l;
			}
		}

		if (memObj != null) {
			MemStatementList l = memObj.getObjectStatementList();
			if (l.size() < smallestList.size()) {
				smallestList = l;
			}
		}

		return new MemStatementIterator<X>(smallestList, memSubj, memPred, memObj, explicitOnly, snapshot,
				readMode, memContexts);
	}

	protected Statement addStatement(Resource subj, URI pred, Value obj, Resource context, boolean explicit)
		throws StoreException
	{
		boolean newValueCreated = false;

		// Get or create MemValues for the operands
		MemResource memSubj = valueFactory.getMemResource(subj);
		if (memSubj == null) {
			memSubj = valueFactory.createMemResource(subj);
			newValueCreated = true;
		}
		MemURI memPred = valueFactory.getMemURI(pred);
		if (memPred == null) {
			memPred = valueFactory.createMemURI(pred);
			newValueCreated = true;
		}
		MemValue memObj = valueFactory.getMemValue(obj);
		if (memObj == null) {
			memObj = valueFactory.createMemValue(obj);
			newValueCreated = true;
		}
		MemResource memContext = valueFactory.getMemResource(context);
		if (context != null && memContext == null) {
			memContext = valueFactory.createMemResource(context);
			newValueCreated = true;
		}

		if (!newValueCreated) {
			// All values were already present in the graph. Possibly, the
			// statement is already present. Check this.
			CloseableIteration<MemStatement, StoreException> stIter = createStatementIterator(
					StoreException.class, memSubj, memPred, memObj, false, currentSnapshot + 1, ReadMode.RAW,
					memContext);

			try {
				if (stIter.hasNext()) {
					// statement is already present, update its transaction
					// status if appropriate
					MemStatement st = stIter.next();

					txnStatements.put(st, st);

					TxnStatus txnStatus = st.getTxnStatus();

					if (txnStatus == TxnStatus.NEUTRAL && !st.isExplicit() && explicit) {
						// Implicit statement is now added explicitly
						st.setTxnStatus(TxnStatus.EXPLICIT);
					}
					else if (txnStatus == TxnStatus.NEW && !st.isExplicit() && explicit) {
						// Statement was first added implicitly and now
						// explicitly
						st.setExplicit(true);
					}
					else if (txnStatus == TxnStatus.DEPRECATED) {
						if (st.isExplicit() == explicit) {
							// Statement was removed but is now re-added
							st.setTxnStatus(TxnStatus.NEUTRAL);
						}
						else if (explicit) {
							// Implicit statement was removed but is now added
							// explicitly
							st.setTxnStatus(TxnStatus.EXPLICIT);
						}
						else {
							// Explicit statement was removed but can still be
							// inferred
							st.setTxnStatus(TxnStatus.INFERRED);
						}

						return st;
					}
					else if (txnStatus == TxnStatus.INFERRED && st.isExplicit() && explicit) {
						// Explicit statement was removed but is now re-added
						st.setTxnStatus(TxnStatus.NEUTRAL);
					}
					else if (txnStatus == TxnStatus.ZOMBIE) {
						// Restore zombie statement
						st.setTxnStatus(TxnStatus.NEW);
						st.setExplicit(explicit);

						return st;
					}

					return null;
				}
			}
			finally {
				stIter.close();
			}
		}

		// completely new statement
		MemStatement st = new MemStatement(memSubj, memPred, memObj, memContext, explicit, currentSnapshot + 1,
				TxnStatus.NEW);
		statements.add(st);
		st.addToComponentLists();

		txnStatements.put(st, st);

		return st;
	}

	protected boolean removeStatement(MemStatement st, boolean explicit)
		throws StoreException
	{
		boolean statementsRemoved = false;
		TxnStatus txnStatus = st.getTxnStatus();

		if (txnStatus == TxnStatus.NEUTRAL && st.isExplicit() == explicit) {
			// Remove explicit statement
			st.setTxnStatus(TxnStatus.DEPRECATED);
			statementsRemoved = true;
		}
		else if (txnStatus == TxnStatus.NEW && st.isExplicit() == explicit) {
			// Statement was added and now removed in the same transaction
			st.setTxnStatus(TxnStatus.ZOMBIE);
			statementsRemoved = true;
		}
		else if (txnStatus == TxnStatus.INFERRED && st.isExplicit() && !explicit) {
			// Explicit statement was replaced by inferred statement and this
			// inferred statement is now removed
			st.setTxnStatus(TxnStatus.DEPRECATED);
			statementsRemoved = true;
		}
		else if (txnStatus == TxnStatus.EXPLICIT && !st.isExplicit() && explicit) {
			// Inferred statement was replaced by explicit statement, but this is
			// now undone
			st.setTxnStatus(TxnStatus.NEUTRAL);
		}

		txnStatements.put(st, st);

		return statementsRemoved;
	}

	protected void startTransaction()
		throws StoreException
	{
		cancelSyncTask();

		txnStatements = new IdentityHashMap<MemStatement, MemStatement>();
	}

	protected void commit()
		throws StoreException
	{
		boolean statementsAdded = false;
		boolean statementsRemoved = false;
		boolean statementsDeprecated = false;

		int txnSnapshot = currentSnapshot + 1;

		for (MemStatement st : txnStatements.keySet()) {
			TxnStatus txnStatus = st.getTxnStatus();

			if (txnStatus == TxnStatus.NEUTRAL) {
				continue;
			}
			else if (txnStatus == TxnStatus.NEW) {
				statementsAdded = true;
			}
			else if (txnStatus == TxnStatus.DEPRECATED) {
				st.setTillSnapshot(txnSnapshot);
				statementsRemoved = true;
			}
			else if (txnStatus == TxnStatus.ZOMBIE) {
				st.setTillSnapshot(txnSnapshot);
				statementsDeprecated = true;
			}
			else if (txnStatus == TxnStatus.EXPLICIT || txnStatus == TxnStatus.INFERRED) {
				// Deprecate the existing statement...
				st.setTillSnapshot(txnSnapshot);
				statementsDeprecated = true;

				// ...and add a clone with modified explicit/implicit flag
				MemStatement explSt = new MemStatement(st.getSubject(), st.getPredicate(), st.getObject(),
						st.getContext(), txnStatus == TxnStatus.EXPLICIT, txnSnapshot);
				statements.add(explSt);
				explSt.addToComponentLists();
			}

			st.setTxnStatus(TxnStatus.NEUTRAL);
		}

		txnStatements = null;

		if (statementsAdded || statementsRemoved || statementsDeprecated) {
			currentSnapshot = txnSnapshot;
		}

		if (statementsAdded || statementsRemoved) {
			contentsChanged = true;
			scheduleSyncTask();

			DefaultSailChangedEvent event = new DefaultSailChangedEvent(this);
			event.setStatementsAdded(statementsAdded);
			event.setStatementsRemoved(statementsRemoved);
			notifySailChanged(event);
		}

		if (statementsDeprecated) {
			scheduleSnapshotCleanup();
		}
	}

	protected void rollback()
		throws StoreException
	{
		logger.debug("rolling back transaction");

		for (MemStatement st : txnStatements.keySet()) {
			TxnStatus txnStatus = st.getTxnStatus();
			if (txnStatus == TxnStatus.NEW || txnStatus == TxnStatus.ZOMBIE) {
				// Statement has been added during this transaction and deprecates immediately
				st.setTillSnapshot(currentSnapshot);
			}
			else if (txnStatus != TxnStatus.NEUTRAL) {
				// Return statement to neutral status
				st.setTxnStatus(TxnStatus.NEUTRAL);
			}
		}

		txnStatements = null;

		scheduleSnapshotCleanup();
	}

	protected void scheduleSyncTask()
		throws StoreException
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
							// Acquire read lock to guarantee that the statement list
							// doesn't change while writing
							Lock stLock = getStatementsReadLock();
							try {
								sync();
							}
							finally {
								stLock.release();
							}
						}
						catch (StoreException e) {
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
		throws StoreException
	{
		synchronized (syncSemaphore) {
			if (persist && contentsChanged) {
				logger.debug("syncing data to file...");
				try {
					FileIO.write(this, syncFile, dataFile);
					contentsChanged = false;
					logger.debug("Data synced to file");
				}
				catch (IOException e) {
					logger.error("Failed to sync to file", e);
					throw new StoreException(e);
				}
			}
		}
	}

	/**
	 * Removes statements from old snapshots from the main statement list and
	 * resets the snapshot to 1 for the rest of the statements.
	 * 
	 * @throws InterruptedException
	 */
	protected void cleanSnapshots()
		throws InterruptedException
	{
		MemStatementList statements = this.statements;

		if (statements == null) {
			// Store has been shut down
			return;
		}

		Lock stLock = statementListLockManager.getWriteLock();
		try {
			for (int i = statements.size() - 1; i >= 0; i--) {
				MemStatement st = statements.get(i);

				if (st.getTillSnapshot() <= currentSnapshot) {
					// stale statement
					st.removeFromComponentLists();
					statements.remove(i);
				}
				else {
					// Reset snapshot
					st.setSinceSnapshot(1);
				}
			}

			currentSnapshot = 1;
		}
		finally {
			stLock.release();
		}
	}

	protected void scheduleSnapshotCleanup() {
		synchronized (snapshotCleanupThreadSemaphore) {
			if (snapshotCleanupThread == null || !snapshotCleanupThread.isAlive()) {
				Runnable runnable = new Runnable() {

					public void run() {
						try {
							cleanSnapshots();
						}
						catch (InterruptedException e) {
							logger.warn("snapshot cleanup interrupted");
						}
					}
				};

				snapshotCleanupThread = new Thread(runnable, "MemoryStore snapshot cleanup");
				snapshotCleanupThread.setDaemon(true);
				snapshotCleanupThread.start();
			}
		}
	}
}
