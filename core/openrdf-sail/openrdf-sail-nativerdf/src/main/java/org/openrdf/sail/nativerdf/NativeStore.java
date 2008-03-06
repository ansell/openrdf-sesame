/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.concurrent.locks.ExclusiveLockManager;
import info.aduna.concurrent.locks.Lock;
import info.aduna.concurrent.locks.MultiReadSingleWriteLockManager;

import org.openrdf.model.ValueFactory;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailInitializationException;
import org.openrdf.sail.helpers.SailBase;

/**
 * A SAIL implementation using B-Tree indexing on disk for storing and querying
 * its data.
 * 
 * @author Arjohn Kampman
 * @author jeen
 */
public class NativeStore extends SailBase {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * Key used to specify which triple indexes to use.
	 */
	public static final String TRIPLES_INDEXES_KEY = "triple-indexes";

	/**
	 * Key used to specify whether synchronization locks should be tracked.
	 */
	public static final String TRACK_LOCKS_KEY = "trackLocks";

	/*-----------*
	 * Variables *
	 *-----------*/

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
	private MultiReadSingleWriteLockManager _lockManager;

	private ExclusiveLockManager _txnLockManager;

	private boolean _trackLocks = false;

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
		if (TRIPLES_INDEXES_KEY.equals(key)) {
			setTripleIndexes(value);
		}
		else if (TRACK_LOCKS_KEY.equals(key)) {
			_trackLocks = "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
		}
		else {
			super.setParameter(key, value);
		}
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
	 * @exception SailException
	 *            If this RdfRepository could not be initialized using the
	 *            parameters that have been set.
	 */
	public void initialize()
		throws SailException
	{
		if (_initialized) {
			throw new IllegalStateException("sail has already been intialized");
		}

		logger.debug("Initializing NativeStore...");

		_lockManager = new MultiReadSingleWriteLockManager(_trackLocks);
		_txnLockManager = new ExclusiveLockManager(_trackLocks);

		// Check initialization parameters
		File dataDir = getDataDir();

		if (dataDir == null) {
			throw new SailInitializationException("Missing parameter: " + DATA_DIR_KEY);
		}
		else if (!dataDir.exists()) {
			boolean success = dataDir.mkdirs();
			if (!success) {
				throw new SailInitializationException("Unable to create data directory: " + dataDir);
			}
		}
		else if (!dataDir.isDirectory()) {
			throw new SailInitializationException("The specified path does not denote a directory: " + dataDir);
		}
		else if (!dataDir.canRead()) {
			throw new SailInitializationException("Not allowed to read from the specified directory: " + dataDir);
		}

		logger.debug("Data dir is " + dataDir);

		try {
			_namespaceStore = new NamespaceStore(dataDir);
			_valueStore = new ValueStore(dataDir);
			_tripleStore = new TripleStore(dataDir, _tripleIndexes);
		}
		catch (IOException e) {
			throw new SailInitializationException(e);
		}

		_initialized = true;
		logger.debug("NativeStore initialized");
	}

	public void shutDown()
		throws SailException
	{
		if (_initialized) {
			logger.debug("Shutting down NativeStore...");

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

						logger.debug("NativeStore shut down");
					}
					catch (IOException e) {
						throw new SailException(e);
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
				logger.warn("Failed to acquire transaction lock", e);
			}
		}
	}

	public boolean isWritable() {
		return getDataDir().canWrite();
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

	TripleStore getTripleStore() {
		return _tripleStore;
	}

	ValueStore getValueStore() {
		return _valueStore;
	}

	NamespaceStore getNamespaceStore() {
		return _namespaceStore;
	}

	Lock getReadLock()
		throws SailException
	{
		try {
			return _lockManager.getReadLock();
		}
		catch (InterruptedException e) {
			throw new SailException(e);
		}
	}

	Lock getWriteLock()
		throws SailException
	{
		try {
			return _lockManager.getWriteLock();
		}
		catch (InterruptedException e) {
			throw new SailException(e);
		}
	}

	Lock getTxnLock()
		throws InterruptedException
	{
		return _txnLockManager.getExclusiveLock();
	}
}
