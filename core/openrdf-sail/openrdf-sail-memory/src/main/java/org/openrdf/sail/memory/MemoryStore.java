/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.Namespace;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailChangedEvent;
import org.openrdf.sail.SailChangedListener;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailInitializationException;
import org.openrdf.sail.SailInternalException;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.memory.model.MemResource;
import org.openrdf.sail.memory.model.MemStatement;
import org.openrdf.sail.memory.model.MemStatementIterator;
import org.openrdf.sail.memory.model.MemStatementList;
import org.openrdf.sail.memory.model.MemURI;
import org.openrdf.sail.memory.model.MemValue;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.openrdf.sail.memory.model.ReadMode;
import org.openrdf.sail.memory.model.TxnStatus;
import org.openrdf.util.IOUtil;
import org.openrdf.util.iterator.CloseableIterator;
import org.openrdf.util.iterator.EmptyIterator;
import org.openrdf.util.locking.ExclusiveLockManager;
import org.openrdf.util.locking.Lock;
import org.openrdf.util.locking.MultiReadSingleWriteLockManager;
import org.openrdf.util.log.ThreadLog;

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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * An implementation of the Sail interface that stores its data in main memory
 * and that can use a file for persistent storage. This Sail implementation
 * supports single, isolated transactions. This means that changes to the data
 * are not visible until a transaction is committed and that concurrent
 * transactions are not possible. When another transaction is active, calls to
 * <tt>startTransaction()</tt> will block until the active transaction is
 * committed or rolled back.
 */
public class MemoryStore implements Sail {

	/*-----------*
	 * Constants *
	 *-----------*/

	/** Key used to specify a file for persistent storage. */
	public static final String FILE_KEY = "file";

	/** Key used to specify a file for persistent storage. */
	public static final String SYNC_DELAY_KEY = "syncDelay";

	/**
	 * Lock manager used to prevent concurrent transactions.
	 */
	private final ExclusiveLockManager _txnLockManager = new ExclusiveLockManager();

	/**
	 * Lock manager used to prevent queries during commits and vice versa.
	 */
	private final MultiReadSingleWriteLockManager _queryLockManager = new MultiReadSingleWriteLockManager();

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Factory/cache for MemValue objects.
	 */
	private MemValueFactory _valueFactory;

	/**
	 * List containing all available statements.
	 */
	private MemStatementList _statements;

	/**
	 * Flag indicating whether the Sail has been initialized.
	 */
	private boolean _initialized = false;

	/**
	 * Flag indicating whether the contents of this repository have changed.
	 */
	private boolean _contentsChanged;

	/**
	 * The file that should be used for persistent storage.
	 */
	private File _file;

	/**
	 * The sync delay.
	 * 
	 * @see #setSyncDelay
	 */
	private long _syncDelay = 0L;

	/**
	 * The timer used to trigger file synchronization.
	 */
	private Timer _syncTimer;

	/**
	 * Objects that should be notified of changes to the data in this Sail.
	 */
	private List<SailChangedListener> _sailChangedListeners = new ArrayList<SailChangedListener>(0);

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new MemoryStore.
	 */
	public MemoryStore() {
	}

	/**
	 * Creates a new persistent MemoryStore. If the supplied file contains an
	 * existing store, its contents will be restored upon initialization.
	 * 
	 * @param file
	 *        the file to be used for persistence.
	 */
	public MemoryStore(File file) {
		_file = file;
	}

	/*---------*
	 * Methods *
	 *---------*/

	// Implements Sail.setParameter(...)
	public void setParameter(String key, String value) {
		if (_initialized) {
			throw new IllegalStateException("sail has already been intialized");
		}

		if (FILE_KEY.equals(key)) {
			_file = new File(value);
		}
		else if (SYNC_DELAY_KEY.equals(key)) {
			try {
				setSyncDelay(Integer.parseInt(value));
			}
			catch (NumberFormatException e) {
				throw new IllegalArgumentException("Illegal (non-integer) value for sync delay : " + value);
			}
		}
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
		_syncDelay = syncDelay;
	}

	/**
	 * Gets the currently configured sync delay.
	 * 
	 * @return syncDelay The sync delay in milliseconds.
	 * @see #setSyncDelay
	 */
	public long getSyncDelay() {
		return _syncDelay;
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
		if (_initialized) {
			throw new IllegalStateException("sail has already been intialized");
		}

		ThreadLog.trace("Initializing MemoryStore...");

		_valueFactory = new MemValueFactory();
		_statements = new MemStatementList(256);

		if (_file != null) {
			if (_file.exists()) {
				ThreadLog.trace("Reading data from " + _file + "...");

				// Initialize persistent store from file
				if (!_file.canRead()) {
					ThreadLog.error("Data file is not readable", _file);
					throw new SailInitializationException("Can't read data file " + _file);
				}
				// Don't try to read empty files: this will result in an
				// IOException, and the file doesn't contain any data anyway.
				if (_file.length() == 0L) {
					ThreadLog.warning("Ignoring empty data file", _file);
				}
				else {
					try {
						_readFromFile();
						ThreadLog.trace("Data file read successfully");
					}
					catch (IOException e) {
						ThreadLog.error("Failed to read data file", e);
						throw new SailInitializationException(e);
					}
				}
			}
			else {
				// file specified that does not exist yet, create it
				try {
					File dir = _file.getParentFile();
					if (dir != null && !dir.exists()) {
						ThreadLog.trace("Creating directory for data file...");
						if (!dir.mkdirs()) {
							ThreadLog.trace("Failed to create directory for data file: " + dir);
							throw new SailInitializationException("Failed to create directory for data file: " + dir);
						}
					}

					ThreadLog.trace("Initializing data file...");
					_writeToFile();
					ThreadLog.trace("Data file initialized");
				}
				catch (IOException e) {
					ThreadLog.trace("Failed to initialize data file", e);
					throw new SailInitializationException("Failed to initialize data file " + _file);
				}
			}
		}

		_contentsChanged = false;
		_initialized = true;

		ThreadLog.trace("MemoryStore initialized");
	}

	// Implements Sail.shutDown()
	public void shutDown() {
		if (_initialized) {
			Lock queryLock = getQueryWriteLock();
			try {
				sync();

				_valueFactory = null;
				_statements = null;
				_initialized = false;
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
		return _file == null || _file.canWrite();
	}

	// Implements Sail.startTransaction()
	public SailConnection getConnection()
		throws SailException
	{
		if (!_initialized) {
			throw new IllegalStateException("sail not initialized.");
		}

		return new MemoryStoreConnection(this);
	}

	// Implements Sail.getValueFactory()
	public MemValueFactory getValueFactory() {
		if (!_initialized) {
			throw new IllegalStateException("sail not initialized.");
		}

		return _valueFactory;
	}

	// Implements Sail.addSailChangedListener(SailChangedListener)
	public void addSailChangedListener(SailChangedListener listener) {
		synchronized (_sailChangedListeners) {
			_sailChangedListeners.add(listener);
		}
	}

	// Implements Sail.removeSailChangedListener(SailChangedListener)
	public void removeSailChangedListener(SailChangedListener listener) {
		synchronized (_sailChangedListeners) {
			_sailChangedListeners.remove(listener);
		}
	}

	/**
	 * Notifies all registered SailChangedListener's of changes to the contents
	 * of this Sail.
	 */
	void notifySailChanged(SailChangedEvent event) {
		synchronized (_sailChangedListeners) {
			for (SailChangedListener l : _sailChangedListeners) {
				l.sailChanged(event);
			}
		}
	}

	Lock getTransactionLock()
		throws InterruptedException
	{
		return _txnLockManager.getExclusiveLock();
	}

	Lock getQueryReadLock() {
		try {
			return _queryLockManager.getReadLock();
		}
		catch (InterruptedException e) {
			throw new SailInternalException(e);
		}
	}

	Lock getQueryWriteLock() {
		try {
			return _queryLockManager.getWriteLock();
		}
		catch (InterruptedException e) {
			throw new SailInternalException(e);
		}
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
	public CloseableIterator<MemStatement> createStatementIterator(Resource subj, URI pred, Value obj,
			Resource context, boolean explicitOnly, boolean namedContextsOnly, ReadMode readMode)
	{
		MemResource memSubj = null;
		MemURI memPred = null;
		MemValue memObj = null;
		MemResource memContext = null;

		// Perform look-ups for value-equivalents of the specified values
		if (subj != null) {
			memSubj = _valueFactory.getMemResource(subj);
			if (memSubj == null) {
				// non-existent subject
				return new EmptyIterator<MemStatement>();
			}
		}
		if (pred != null) {
			memPred = _valueFactory.getMemURI(pred);
			if (memPred == null) {
				// non-existent predicate
				return new EmptyIterator<MemStatement>();
			}
		}
		if (obj != null) {
			memObj = _valueFactory.getMemValue(obj);
			if (memObj == null) {
				// non-existent object
				return new EmptyIterator<MemStatement>();
			}
		}
		if (context != null) {
			memContext = _valueFactory.getMemResource(context);
			if (memContext == null) {
				// non-existent context
				return new EmptyIterator<MemStatement>();
			}
		}

		// Search for the smallest list that can be used by the iterator
		MemStatementList smallestList = _statements;

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

		// Return an iterator that iterates over 'smallestList'
		return new MemStatementIterator(smallestList, memSubj, memPred, memObj, memContext, explicitOnly,
				namedContextsOnly, readMode);
	}

	Statement addStatement(Resource subj, URI pred, Value obj, Resource context, boolean explicit) {
		boolean newValueCreated = false;

		// Get or create MemValues for the operands
		MemResource memSubj = _valueFactory.getMemResource(subj);
		if (memSubj == null) {
			memSubj = _valueFactory.createMemResource(subj);
			newValueCreated = true;
		}
		MemURI memPred = _valueFactory.getMemURI(pred);
		if (memPred == null) {
			memPred = _valueFactory.createMemURI(pred);
			newValueCreated = true;
		}
		MemValue memObj = _valueFactory.getMemValue(obj);
		if (memObj == null) {
			memObj = _valueFactory.createMemValue(obj);
			newValueCreated = true;
		}
		MemResource memContext = _valueFactory.getMemResource(context);
		if (memContext == null) {
			memContext = _valueFactory.createMemResource(context);
			newValueCreated = true;
		}

		if (!newValueCreated) {
			// All values were already present in the graph. Possibly, the
			// statement is already present. Check this.
			CloseableIterator<MemStatement> stIter = createStatementIterator(memSubj, memPred, memObj,
					memContext, false, false, ReadMode.RAW);

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
		_statements.add(st);
		st.addToComponentLists();

		return st;
	}

	void commit() {
		boolean statementsAdded = false;
		boolean statementsRemoved = false;

		for (int i = _statements.size() - 1; i >= 0; i--) {
			MemStatement st = _statements.get(i);
			TxnStatus txnStatus = st.getTxnStatus();

			if (txnStatus == TxnStatus.NEW) {
				st.setTxnStatus(TxnStatus.NEUTRAL);
				statementsAdded = true;
			}
			else if (txnStatus == TxnStatus.DEPRECATED || txnStatus == TxnStatus.ZOMBIE) {
				_statements.remove(i);
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
			_contentsChanged = true;
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
		ThreadLog.trace("rolling back transaction");

		for (int i = _statements.size() - 1; i >= 0; i--) {
			MemStatement st = _statements.get(i);

			TxnStatus txnStatus = st.getTxnStatus();
			if (txnStatus == TxnStatus.NEW || txnStatus == TxnStatus.ZOMBIE) {
				// Statement has been added during this transaction, remove it
				_statements.remove(i);
				st.removeFromComponentLists();
			}
			else if (txnStatus != TxnStatus.NEUTRAL) {
				// Return statement to neutral status
				st.setTxnStatus(TxnStatus.NEUTRAL);
			}
		}
	}

	synchronized void startSyncTimer() {
		if (_syncDelay == 0L) {
			// Sync immediately
			sync();
		}
		else if (_syncDelay > 0L) {
			// Sync in _syncDelay milliseconds
			_syncTimer = new Timer();
			TimerTask tt = new TimerTask() {

				public void run() {
					Lock queryLock = getQueryReadLock();
					try {
						sync();
					}
					finally {
						queryLock.release();
					}
				}
			};
			_syncTimer.schedule(tt, _syncDelay);
		}
	}

	synchronized void stopSyncTimer() {
		if (_syncTimer != null) {
			_syncTimer.cancel();
			_syncTimer = null;
		}
	}

	/**
	 * Synchronizes the contents of this repository with the data that is stored
	 * on disk. Data will only be written when the contents of the repository and
	 * data in the file are out of sync.
	 */
	public void sync() {
		if (_contentsChanged && _file != null) {
			ThreadLog.trace("syncing data to file...");
			try {
				_writeToFile();
				_contentsChanged = false;
				ThreadLog.trace("Data synced to file");
			}
			catch (IOException e) {
				ThreadLog.error("Failed to sync to file", e);
				throw new SailInternalException(e);
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

	private void _writeToFile()
		throws IOException
	{
		OutputStream out = new FileOutputStream(_file);
		try {
			// Write header
			out.write(MAGIC_NUMBER);
			out.write(BMSF_VERSION);

			// The rest of the data is GZIP-compressed
			DataOutputStream dataOut = new DataOutputStream(new GZIPOutputStream(out));
			out = dataOut;

			_writeNamespaces(dataOut);

			_writeStatements(dataOut);

			dataOut.writeByte(EOF_MARKER);
		}
		finally {
			out.close();
		}
	}

	private void _readFromFile()
		throws IOException
	{
		InputStream in = new FileInputStream(_file);
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
						_readNamespace(dataIn);
						break;
					case EXPL_TRIPLE_MARKER:
						_readStatement(false, true, dataIn);
						break;
					case EXPL_QUAD_MARKER:
						_readStatement(true, true, dataIn);
						break;
					case INF_TRIPLE_MARKER:
						_readStatement(false, false, dataIn);
						break;
					case INF_QUAD_MARKER:
						_readStatement(true, false, dataIn);
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

	private void _writeNamespaces(DataOutputStream dataOut)
		throws IOException
	{
		for (Namespace ns : _valueFactory.getNamespaces()) {
			dataOut.writeByte(NAMESPACE_MARKER);
			dataOut.writeUTF(ns.getPrefix());
			dataOut.writeUTF(ns.getName());

			// FIXME dummy boolean to be compatible with older version:
			// the up-to-date status is no longer relevant
			dataOut.writeBoolean(true);
		}
	}

	private void _readNamespace(DataInputStream dataIn)
		throws IOException
	{
		String prefix = dataIn.readUTF();
		String name = dataIn.readUTF();

		// FIXME dummy boolean to be compatible with older version:
		// the up-to-date status is no longer relevant
		dataIn.readBoolean();

		_valueFactory.registerNamespace(prefix, name);
	}

	private void _writeStatements(DataOutputStream dataOut)
		throws IOException
	{
		CloseableIterator<MemStatement> stIter = createStatementIterator(null, null, null, null, false, false,
				ReadMode.COMMITTED);

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

				_writeValue(st.getSubject(), dataOut);
				_writeValue(st.getPredicate(), dataOut);
				_writeValue(st.getObject(), dataOut);
				if (context != null) {
					_writeValue(context, dataOut);
				}
			}
		}
		finally {
			stIter.close();
		}
	}

	private void _readStatement(boolean hasContext, boolean isExplicit, DataInputStream dataIn)
		throws IOException, ClassCastException
	{
		MemResource memSubj = (MemResource)_readValue(dataIn);
		MemURI memPred = (MemURI)_readValue(dataIn);
		MemValue memObj = (MemValue)_readValue(dataIn);
		MemResource memContext = _valueFactory.getNullContext();
		if (hasContext) {
			memContext = (MemResource)_readValue(dataIn);
		}

		MemStatement st = new MemStatement(memSubj, memPred, memObj, memContext, isExplicit);
		_statements.add(st);
		st.addToComponentLists();
		st.setTxnStatus(TxnStatus.NEUTRAL);
	}

	private void _writeValue(Value value, DataOutputStream dataOut)
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
				_writeValue(datatype, dataOut);
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

	private Value _readValue(DataInputStream dataIn)
		throws IOException, ClassCastException
	{
		int valueTypeMarker = dataIn.readByte();

		if (valueTypeMarker == URI_MARKER) {
			String uriString = dataIn.readUTF();
			return _valueFactory.createURI(uriString);
		}
		else if (valueTypeMarker == BNODE_MARKER) {
			String bnodeID = dataIn.readUTF();
			return _valueFactory.createBNode(bnodeID);
		}
		else if (valueTypeMarker == PLAIN_LITERAL_MARKER) {
			String label = dataIn.readUTF();
			return _valueFactory.createLiteral(label);
		}
		else if (valueTypeMarker == LANG_LITERAL_MARKER) {
			String label = dataIn.readUTF();
			String language = dataIn.readUTF();
			return _valueFactory.createLiteral(label, language);
		}
		else if (valueTypeMarker == DATATYPE_LITERAL_MARKER) {
			String label = dataIn.readUTF();
			URI datatype = (URI)_readValue(dataIn);
			return _valueFactory.createLiteral(label, (URI)datatype);
		}
		else {
			throw new IOException("Invalid value type marker: " + valueTypeMarker);
		}
	}
}
