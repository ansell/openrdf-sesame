/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailChangedEvent;
import org.openrdf.sail.SailChangedListener;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailInitializationException;
import org.openrdf.sail.SailInternalException;
import org.openrdf.util.locking.ExclusiveLockManager;
import org.openrdf.util.locking.Lock;
import org.openrdf.util.locking.MultiReadSingleWriteLockManager;
import org.openrdf.util.log.ThreadLog;

/**
 * A SAIL implementation using B-Tree indexing on disk for storing and querying
 * its data.
 * 
 * @author arjohn
 * @author jeen
 */
public class NativeStore implements Sail {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * Key used to specify a data directory in the initialization parameters.
	 */
	public static final String DATA_DIR_KEY = "dir";

	/**
	 * Key used to specify which triple indexes to use.
	 */
	public static final String TRIPLES_INDEXES_KEY = "triple-indexes";

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The directory where all data files for this native store are stored.
	 */
	private File _dataDir;

	/**
	 * Specifies which triple indexes this native store must use.
	 */
	private String _tripleIndexes;

	TripleStore _tripleStore;

	ValueStore _valueStore;

	NamespaceStore _namespaceStore;

	/**
	 * Lock manager used to prevent queries during commits and vice versa.
	 */
	private MultiReadSingleWriteLockManager _lockManager = new MultiReadSingleWriteLockManager();

	private ExclusiveLockManager _txnLockManager = new ExclusiveLockManager();

	private List<SailChangedListener> _sailChangedListeners;

	/**
	 * Flag indicating whether the Sail has been initialized.
	 */
	private boolean _initialized;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new NativeStore.
	 */
	public NativeStore() {
		_initialized = false;

		_sailChangedListeners = new ArrayList<SailChangedListener>(0);
	}

	public NativeStore(File dataDir) {
		this();
		setDataDir(dataDir);
	}

	public NativeStore(File dataDir, String tripleIndexes) {
		this(dataDir);
		setTripleIndexes(tripleIndexes);
	}

	/*---------*
	 * Methods *
	 *---------*/

	// Implements Sail.setParameter(...)
	public void setParameter(String key, String value) {
		if (DATA_DIR_KEY.equals(key)) {
			setDataDir(new File(value));
		}
		else if (TRIPLES_INDEXES_KEY.equals(key)) {
			setTripleIndexes(value);
		}
	}

	public void setDataDir(File dataDir) {
		if (_initialized) {
			throw new IllegalStateException("sail has already been intialized");
		}

		_dataDir = dataDir;
	}

	public File getDataDir() {
		return _dataDir;
	}

	public void setTripleIndexes(String tripleIndexes) {
		if (_initialized) {
			throw new IllegalStateException("sail has already been intialized");
		}

		_tripleIndexes = tripleIndexes;
	}

	public String getTripleIndexes() {
		return _tripleIndexes;
	}

	/**
	 * Initializes this NativeStore.
	 * 
	 * @exception SailInitializationException
	 *            If this RdfRepository could not be initialized using the
	 *            parameters that have been set.
	 */
	public void initialize()
		throws SailInitializationException
	{
		if (_initialized) {
			throw new IllegalStateException("sail has already been intialized");
		}

		ThreadLog.trace("Initializing NativeStore...");

		// Check initialization parameters
		if (_dataDir == null) {
			throw new SailInitializationException("Missing parameter: " + DATA_DIR_KEY);
		}
		else if (!_dataDir.exists()) {
			boolean success = _dataDir.mkdirs();
			if (!success) {
				throw new SailInitializationException("Unable to create data directory: " + _dataDir);
			}
		}
		else if (!_dataDir.isDirectory()) {
			throw new SailInitializationException("The specified path does not denote a directory: " + _dataDir);
		}
		else if (!_dataDir.canRead()) {
			throw new SailInitializationException("Not allowed to read from the specified directory: "
					+ _dataDir);
		}

		ThreadLog.trace("Data dir is " + _dataDir);

		try {
			_namespaceStore = new NamespaceStore(_dataDir);
			_valueStore = new ValueStore(_dataDir);
			_tripleStore = new TripleStore(_dataDir, _tripleIndexes);
		}
		catch (IOException e) {
			throw new SailInitializationException(e);
		}

		_initialized = true;
		ThreadLog.trace("NativeStore initialized");
	}

	// Implements Sail.shutDown()
	public void shutDown() {
		if (_initialized) {
			ThreadLog.trace("Shutting down NativeStore...");

			try {
				// Get txn lock to make sure no transaction are active
				Lock txnLock = getTxnLock();

				try {
					// Get write lock to make sure no connections are querying
					Lock writeLock = getWriteLock();
					try {
						_tripleStore.close();
						_valueStore.close();
						_namespaceStore.close();

						_initialized = false;

						ThreadLog.trace("NativeStore shut down");
					}
					catch (IOException e) {
						throw new SailInternalException(e);
					}
					finally {
						writeLock.release();
					}
				}
				finally {
					txnLock.release();
				}
			}
			catch (InterruptedException e) {
				ThreadLog.warning("Failed to acquire transaction lock", e);
			}
		}
	}

	public boolean isWritable() {
		return _dataDir.canWrite();
	}

	public SailConnection getConnection()
		throws SailException
	{
		if (!_initialized) {
			throw new IllegalStateException("sail not initialized.");
		}

		try {
			return new NativeStoreConnection(this);
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	// Implements RdfSource.getValueFactory()
	public ValueFactory getValueFactory() {
		return _valueStore;
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
	void _notifySailChanged(SailChangedEvent event) {
		synchronized (_sailChangedListeners) {
			for (SailChangedListener l : _sailChangedListeners) {
				l.sailChanged(event);
			}
		}
	}

	TripleStore getTripleStore() {
		return _tripleStore;
	}

	ValueStore getValueStore() {
		return _valueStore;
	}

	NamespaceStore getNamespaceStore() {
		return _namespaceStore;
	}

	Lock getReadLock() {
		try {
			return _lockManager.getReadLock();
		}
		catch (InterruptedException e) {
			throw new SailInternalException(e);
		}
	}

	Lock getWriteLock() {
		try {
			return _lockManager.getWriteLock();
		}
		catch (InterruptedException e) {
			throw new SailInternalException(e);
		}
	}

	Lock getTxnLock()
		throws InterruptedException
	{
		return _txnLockManager.getExclusiveLock();
	}
}
