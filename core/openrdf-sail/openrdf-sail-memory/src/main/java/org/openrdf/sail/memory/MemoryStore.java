/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import info.aduna.concurrent.locks.ExclusiveLockManager;
import info.aduna.concurrent.locks.Lock;
import info.aduna.concurrent.locks.MultiReadSingleWriteLockManager;
import info.aduna.io.IOUtil;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.UnionIteration;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailInitializationException;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.helpers.SailBase;
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
public class MemoryStore extends SailBase {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final String PERSISTENT_STORAGE = "memorystore.data";

	/** Key used to specify if the memory store should be persisted. */
	@Deprecated
	public static final String PERSIST_KEY = "persist";

	/** Key used to specify a file for persistent storage. */
	@Deprecated
	public static final String SYNC_DELAY_KEY = "syncDelay";

	/** Key used to specify whether synchronization locks should be tracked. */
	@Deprecated
	public static final String TRACK_LOCKS_KEY = "trackLocks";

	/**
	 * Lock manager used to prevent concurrent transactions.
	 */
	private ExclusiveLockManager txnLockManager;

	/**
	 * Lock manager used to prevent queries during commits and vice versa.
	 */
	private MultiReadSingleWriteLockManager queryLockManager;

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
	 * Store for namespace prefix info.
	 */
	private MemNamespaceStore namespaceStore;

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
	 * The timer used to trigger file synchronization.
	 */
	private Timer syncTimer;

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
	public void setParameter(String key, String value) {
		if (initialized) {
			throw new IllegalStateException("sail has already been initialized");
		}

		if (PERSIST_KEY.equals(key)) {
			setPersist("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value));
		}
		else if (SYNC_DELAY_KEY.equals(key)) {
			try {
				setSyncDelay(Integer.parseInt(value));
			}
			catch (NumberFormatException e) {
				throw new IllegalArgumentException("Illegal (non-integer) value for sync delay : " + value);
			}
		}
		else if (TRACK_LOCKS_KEY.equals(key)) {
			trackLocks = "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
		}
		else {
			super.setParameter(key, value);
		}
	}

	@Override
	public void setDataDir(File dataDir)
	{
		if (initialized) {
			throw new IllegalStateException("sail has already been initialized");
		}

		super.setDataDir(dataDir);
	}

	public void setPersist(boolean persist) {
		if (initialized) {
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
		if (initialized) {
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
	 * @throws SailInitializationException
	 *         when initialization of the store failed.
	 */
	public void initialize()
		throws SailInitializationException
	{
		if (initialized) {
			throw new IllegalStateException("sail has already been intialized");
		}

		logger.debug("Initializing MemoryStore...");

		txnLockManager = new ExclusiveLockManager(trackLocks);
		queryLockManager = new MultiReadSingleWriteLockManager(trackLocks);
		namespaceStore = new MemNamespaceStore();

		valueFactory = new MemValueFactory();
		statements = new MemStatementList(256);

		if (persist) {
			dataFile = new File(getDataDir(), PERSISTENT_STORAGE);

			if (dataFile.exists()) {
				logger.debug("Reading data from {}...", dataFile);

				// Initialize persistent store from file
				if (!dataFile.canRead()) {
					logger.error("Data file is not readable: {}", dataFile);
					throw new SailInitializationException("Can't read data file: " + dataFile);
				}
				// Don't try to read empty files: this will result in an
				// IOException, and the file doesn't contain any data anyway.
				if (dataFile.length() == 0L) {
					logger.warn("Ignoring empty data file: {}", dataFile);
				}
				else {
					try {
						readFromFile(dataFile);
						logger.debug("Data file read successfully");
					}
					catch (IOException e) {
						logger.error("Failed to read data file", e);
						throw new SailInitializationException(e);
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
							throw new SailInitializationException("Failed to create directory for data file: " + dir);
						}
					}

					logger.debug("Initializing data file...");
					writeToFile(dataFile);
					logger.debug("Data file initialized");
				}
				catch (IOException e) {
					logger.debug("Failed to initialize data file", e);
					throw new SailInitializationException("Failed to initialize data file " + dataFile, e);
				}
				catch (SailException e) {
					logger.debug("Failed to initialize data file", e);
					throw new SailInitializationException("Failed to initialize data file " + dataFile, e);
				}
			}
		}

		contentsChanged = false;
		initialized = true;

		logger.debug("MemoryStore initialized");
	}

	@Override
	protected void shutDownInternal()
		throws SailException
	{
		if (initialized) {
			Lock queryLock = getQueryWriteLock();
			try {
				sync();

				valueFactory = null;
				statements = null;
				dataFile = null;
				initialized = false;
			}
			finally {
				queryLock.release();
			}
		}
	}

	/**
	 * Checks whether this Sail object is writable. A MemoryStore is not writable
	 * if a read-only data file is used.
	 */
	public boolean isWritable() {
		// Sail is not writable when it has a data file that is not writable
		return dataFile == null || dataFile.canWrite();
	}

	@Override
	protected SailConnection getConnectionInternal()
		throws SailException
	{
		if (!initialized) {
			throw new IllegalStateException("sail not initialized.");
		}

		return new MemoryStoreConnection(this);
	}

	// Implements Sail.getValueFactory()
	public MemValueFactory getValueFactory() {
		if (!initialized) {
			throw new IllegalStateException("sail not initialized.");
		}

		return valueFactory;
	}

	MemNamespaceStore getNamespaceStore() {
		return namespaceStore;
	}

	MemStatementList getStatements() {
		return statements;
	}

	Lock getTransactionLock()
		throws SailException
	{
		try {
			return txnLockManager.getExclusiveLock();
		}
		catch (InterruptedException e) {
			throw new SailException(e);
		}
	}

	Lock getQueryReadLock()
		throws SailException
	{
		try {
			return queryLockManager.getReadLock();
		}
		catch (InterruptedException e) {
			throw new SailException(e);
		}
	}

	Lock getQueryWriteLock()
		throws SailException
	{
		try {
			return queryLockManager.getWriteLock();
		}
		catch (InterruptedException e) {
			throw new SailException(e);
		}
	}

	int size() {
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
	// FIXME: should not be public
	public <X extends Exception> CloseableIteration<MemStatement, X> createStatementIterator(
			Class<X> excClass, Resource subj, URI pred, Value obj, boolean explicitOnly, ReadMode readMode,
			Resource... contexts)
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		MemResource memSubj = null;
		MemURI memPred = null;
		MemValue memObj = null;
		ArrayList<MemResource> memContextList = null;

		// Perform look-ups for value-equivalents of the specified values
		if (subj != null) {
			memSubj = valueFactory.getMemResource(subj);
			if (memSubj == null) {
				// non-existent subject
				return new EmptyIteration<MemStatement, X>();
			}
		}
		if (pred != null) {
			memPred = valueFactory.getMemURI(pred);
			if (memPred == null) {
				// non-existent predicate
				return new EmptyIteration<MemStatement, X>();
			}
		}
		if (obj != null) {
			memObj = valueFactory.getMemValue(obj);
			if (memObj == null) {
				// non-existent object
				return new EmptyIteration<MemStatement, X>();
			}
		}
		if (contexts.length > 0) {
			memContextList = new ArrayList<MemResource>(contexts.length);
			MemResource memContext = null;
			for (Resource context : contexts) {
				memContext = valueFactory.getMemResource(context);
				if (memContext == null && context != null) {
					// unknown context, skip.
					continue;
				}
				memContextList.add(memContext);
			}
		}

		// Search for the smallest list that can be used by the iterator
		MemStatementList smallestList = null;

		if (memContextList != null) { // contexts specified
			if (memContextList.size() == 0) {
				// no known contexts specified
				return new EmptyIteration<MemStatement, X>();
			}
			else {
				ArrayList<MemStatementIterator<X>> perContextIters = new ArrayList<MemStatementIterator<X>>(
						memContextList.size());

				for (MemResource memContext : memContextList) {
					// reset for each iteration.
					smallestList = statements;

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
					if (memContext != null) {
						MemStatementList l = memContext.getContextStatementList();
						if (l.size() < smallestList.size()) {
							smallestList = l;
						}
					}
					perContextIters.add(new MemStatementIterator<X>(smallestList, memSubj, memPred, memObj,
							explicitOnly, readMode, memContext));
				} // end for

				CloseableIteration<MemStatement, X> result = new UnionIteration<MemStatement, X>(
						perContextIters.toArray((Iteration<? extends MemStatement, X>[])new MemStatementIterator[0]));
				return result;
			}
		}
		else { // no contexts specified, simply search triple patterns
			smallestList = statements;
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
			return new MemStatementIterator<X>(smallestList, memSubj, memPred, memObj, explicitOnly, readMode);
		}
	}

	Statement addStatement(Resource subj, URI pred, Value obj, Resource context, boolean explicit)
		throws SailException
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
		MemResource memContext = null;
		if (context != null) {
			memContext = valueFactory.getMemResource(context);
			if (memContext == null) {
				memContext = valueFactory.createMemResource(context);
				newValueCreated = true;
			}
		}

		if (!newValueCreated) {
			// All values were already present in the graph. Possibly, the
			// statement is already present. Check this.
			CloseableIteration<MemStatement, SailException> stIter = createStatementIterator(
					SailException.class, memSubj, memPred, memObj, false, ReadMode.RAW, memContext);

			try {
				if (stIter.hasNext()) {
					// statement is already present, update its transaction
					// status if appropriate
					MemStatement st = stIter.next();
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
		MemStatement st = new MemStatement(memSubj, memPred, memObj, memContext, explicit);
		statements.add(st);
		st.addToComponentLists();

		return st;
	}

	void commit()
		throws SailException
	{
		boolean statementsAdded = false;
		boolean statementsRemoved = false;

		for (int i = statements.size() - 1; i >= 0; i--) {
			MemStatement st = statements.get(i);
			TxnStatus txnStatus = st.getTxnStatus();

			if (txnStatus == TxnStatus.NEW) {
				st.setTxnStatus(TxnStatus.NEUTRAL);
				statementsAdded = true;
			}
			else if (txnStatus == TxnStatus.DEPRECATED || txnStatus == TxnStatus.ZOMBIE) {
				statements.remove(i);
				st.removeFromComponentLists();
				statementsRemoved = true;
			}
			else if (txnStatus == TxnStatus.EXPLICIT) {
				st.setExplicit(true);
				st.setTxnStatus(TxnStatus.NEUTRAL);
			}
			else if (txnStatus == TxnStatus.INFERRED) {
				st.setExplicit(false);
				st.setTxnStatus(TxnStatus.NEUTRAL);
			}
		}

		if (statementsAdded || statementsRemoved) {
			contentsChanged = true;
			startSyncTimer();

			DefaultSailChangedEvent event = new DefaultSailChangedEvent(this);
			event.setStatementsAdded(statementsAdded);
			event.setStatementsRemoved(statementsRemoved);
			notifySailChanged(event);
		}
	}

	void rollback()
		throws SailException
	{
		logger.debug("rolling back transaction");

		for (int i = statements.size() - 1; i >= 0; i--) {
			MemStatement st = statements.get(i);

			TxnStatus txnStatus = st.getTxnStatus();
			if (txnStatus == TxnStatus.NEW || txnStatus == TxnStatus.ZOMBIE) {
				// Statement has been added during this transaction, remove it
				statements.remove(i);
				st.removeFromComponentLists();
			}
			else if (txnStatus != TxnStatus.NEUTRAL) {
				// Return statement to neutral status
				st.setTxnStatus(TxnStatus.NEUTRAL);
			}
		}
	}

	synchronized void startSyncTimer()
		throws SailException
	{
		if (syncDelay == 0L) {
			// Sync immediately
			sync();
		}
		else if (syncDelay > 0L) {
			// Sync in syncDelay milliseconds
			syncTimer = new Timer();
			TimerTask tt = new TimerTask() {

				public void run() {
					try {
						Lock queryLock = getQueryReadLock();
						try {
							sync();
						}
						finally {
							queryLock.release();
						}
					}
					catch (SailException e) {
						logger.warn("Unable to sync on timer", e);
					}
				}
			};
			syncTimer.schedule(tt, syncDelay);
		}
	}

	synchronized void stopSyncTimer() {
		if (syncTimer != null) {
			syncTimer.cancel();
			syncTimer = null;
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
		if (persist && contentsChanged) {
			logger.debug("syncing data to file...");
			try {
				writeToFile(dataFile);
				contentsChanged = false;
				logger.debug("Data synced to file");
			}
			catch (IOException e) {
				logger.error("Failed to sync to file", e);
				throw new SailException(e);
			}
		}
	}

	/** Magic number for Binary Memory Store Files */
	private static final byte[] MAGIC_NUMBER = new byte[] { 'B', 'M', 'S', 'F' };

	/** The version number of the current format. */
	public static final int BMSF_VERSION = 1;

	/* RECORD TYPES */
	public static final int NAMESPACE_MARKER = 1;

	public static final int EXPL_TRIPLE_MARKER = 2;

	public static final int EXPL_QUAD_MARKER = 3;

	public static final int INF_TRIPLE_MARKER = 4;

	public static final int INF_QUAD_MARKER = 5;

	public static final int URI_MARKER = 6;

	public static final int BNODE_MARKER = 7;

	public static final int PLAIN_LITERAL_MARKER = 8;

	public static final int LANG_LITERAL_MARKER = 9;

	public static final int DATATYPE_LITERAL_MARKER = 10;

	public static final int EOF_MARKER = 127;

	private void writeToFile(File dataFile)
		throws IOException, SailException
	{
		OutputStream out = new FileOutputStream(dataFile);
		try {
			// Write header
			out.write(MAGIC_NUMBER);
			out.write(BMSF_VERSION);

			// The rest of the data is GZIP-compressed
			DataOutputStream dataOut = new DataOutputStream(new GZIPOutputStream(out));
			out = dataOut;

			writeNamespaces(dataOut);

			writeStatements(dataOut);

			dataOut.writeByte(EOF_MARKER);
		}
		finally {
			out.close();
		}
	}

	private void readFromFile(File dataFile)
		throws IOException
	{
		InputStream in = new FileInputStream(dataFile);
		try {
			byte[] magicNumber = IOUtil.readBytes(in, MAGIC_NUMBER.length);
			if (!Arrays.equals(magicNumber, MAGIC_NUMBER)) {
				throw new IOException("File is not a binary MemoryStore file");
			}

			int version = in.read();
			if (version != BMSF_VERSION) {
				throw new IOException("Incompatible format version: " + version);
			}

			// The rest of the data is GZIP-compressed
			DataInputStream dataIn = new DataInputStream(new GZIPInputStream(in));
			in = dataIn;

			int recordTypeMarker;
			while ((recordTypeMarker = dataIn.readByte()) != EOF_MARKER) {
				switch (recordTypeMarker) {
					case NAMESPACE_MARKER:
						readNamespace(dataIn);
						break;
					case EXPL_TRIPLE_MARKER:
						readStatement(false, true, dataIn);
						break;
					case EXPL_QUAD_MARKER:
						readStatement(true, true, dataIn);
						break;
					case INF_TRIPLE_MARKER:
						readStatement(false, false, dataIn);
						break;
					case INF_QUAD_MARKER:
						readStatement(true, false, dataIn);
						break;
					default:
						throw new IOException("Invalid record type marker: " + recordTypeMarker);
				}
			}
		}
		finally {
			in.close();
		}
	}

	private void writeNamespaces(DataOutputStream dataOut)
		throws IOException
	{
		for (Namespace ns : namespaceStore) {
			dataOut.writeByte(NAMESPACE_MARKER);
			dataOut.writeUTF(ns.getPrefix());
			dataOut.writeUTF(ns.getName());

			// FIXME dummy boolean to be compatible with older version:
			// the up-to-date status is no longer relevant
			dataOut.writeBoolean(true);
		}
	}

	private void readNamespace(DataInputStream dataIn)
		throws IOException
	{
		String prefix = dataIn.readUTF();
		String name = dataIn.readUTF();

		// FIXME dummy boolean to be compatible with older version:
		// the up-to-date status is no longer relevant
		dataIn.readBoolean();

		namespaceStore.setNamespace(prefix, name);
	}

	private void writeStatements(DataOutputStream dataOut)
		throws IOException, SailException
	{
		CloseableIteration<MemStatement, SailException> stIter = createStatementIterator(SailException.class,
				null, null, null, false, ReadMode.COMMITTED);

		try {
			while (stIter.hasNext()) {
				MemStatement st = stIter.next();
				Resource context = st.getContext();

				if (st.isExplicit()) {
					if (context == null) {
						dataOut.writeByte(EXPL_TRIPLE_MARKER);
					}
					else {
						dataOut.writeByte(EXPL_QUAD_MARKER);
					}
				}
				else {
					if (context == null) {
						dataOut.writeByte(INF_TRIPLE_MARKER);
					}
					else {
						dataOut.writeByte(INF_QUAD_MARKER);
					}
				}

				writeValue(st.getSubject(), dataOut);
				writeValue(st.getPredicate(), dataOut);
				writeValue(st.getObject(), dataOut);
				if (context != null) {
					writeValue(context, dataOut);
				}
			}
		}
		finally {
			stIter.close();
		}
	}

	private void readStatement(boolean hasContext, boolean isExplicit, DataInputStream dataIn)
		throws IOException, ClassCastException
	{
		MemResource memSubj = (MemResource)readValue(dataIn);
		MemURI memPred = (MemURI)readValue(dataIn);
		MemValue memObj = (MemValue)readValue(dataIn);
		MemResource memContext = null;
		if (hasContext) {
			memContext = (MemResource)readValue(dataIn);
		}

		MemStatement st = new MemStatement(memSubj, memPred, memObj, memContext, isExplicit);
		statements.add(st);
		st.addToComponentLists();
		st.setTxnStatus(TxnStatus.NEUTRAL);
	}

	private void writeValue(Value value, DataOutputStream dataOut)
		throws IOException
	{
		if (value instanceof URI) {
			dataOut.writeByte(URI_MARKER);
			dataOut.writeUTF(((URI)value).toString());
		}
		else if (value instanceof BNode) {
			dataOut.writeByte(BNODE_MARKER);
			dataOut.writeUTF(((BNode)value).getID());
		}
		else if (value instanceof Literal) {
			Literal lit = (Literal)value;

			String label = lit.getLabel();
			String language = lit.getLanguage();
			URI datatype = lit.getDatatype();

			if (datatype != null) {
				dataOut.writeByte(DATATYPE_LITERAL_MARKER);
				dataOut.writeUTF(label);
				writeValue(datatype, dataOut);
			}
			else if (language != null) {
				dataOut.writeByte(LANG_LITERAL_MARKER);
				dataOut.writeUTF(label);
				dataOut.writeUTF(language);
			}
			else {
				dataOut.writeByte(PLAIN_LITERAL_MARKER);
				dataOut.writeUTF(label);
			}
		}
		else {
			throw new IllegalArgumentException("unexpected value type: " + value.getClass());
		}
	}

	private Value readValue(DataInputStream dataIn)
		throws IOException, ClassCastException
	{
		int valueTypeMarker = dataIn.readByte();

		if (valueTypeMarker == URI_MARKER) {
			String uriString = dataIn.readUTF();
			return valueFactory.createURI(uriString);
		}
		else if (valueTypeMarker == BNODE_MARKER) {
			String bnodeID = dataIn.readUTF();
			return valueFactory.createBNode(bnodeID);
		}
		else if (valueTypeMarker == PLAIN_LITERAL_MARKER) {
			String label = dataIn.readUTF();
			return valueFactory.createLiteral(label);
		}
		else if (valueTypeMarker == LANG_LITERAL_MARKER) {
			String label = dataIn.readUTF();
			String language = dataIn.readUTF();
			return valueFactory.createLiteral(label, language);
		}
		else if (valueTypeMarker == DATATYPE_LITERAL_MARKER) {
			String label = dataIn.readUTF();
			URI datatype = (URI)readValue(dataIn);
			return valueFactory.createLiteral(label, datatype);
		}
		else {
			throw new IOException("Invalid value type marker: " + valueTypeMarker);
		}
	}
}
