/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import info.aduna.concurrent.locks.ExclusiveLockManager;
import info.aduna.concurrent.locks.Lock;
import info.aduna.concurrent.locks.MultiReadSingleWriteLockManager;

import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.sail.nativerdf.model.NativeValue;

/**
 * A SAIL implementation using B-Tree indexing on disk for storing and querying
 * its data.
 * 
 * @author Arjohn Kampman
 * @author jeen
 */
public class NativeStore extends SailBase {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * Key used to specify which triple indexes to use.
	 */
	@Deprecated
	public static final String TRIPLES_INDEXES_KEY = "triple-indexes";

	/**
	 * Key used to specify whether synchronization locks should be tracked.
	 */
	@Deprecated
	public static final String TRACK_LOCKS_KEY = "trackLocks";

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Specifies which triple indexes this native store must use.
	 */
	private String tripleIndexes;

	/**
	 * Flag indicating whether updates should be synced to disk forcefully. This
	 * may have a severe impact on write performance. By default, this feature is
	 * disabled.
	 */
	private boolean forceSync = false;

	private TripleStore tripleStore;

	private ValueStore valueStore;

	private NamespaceStore namespaceStore;

	/**
	 * Lock manager used to prevent queries during commits and vice versa.
	 */
	private MultiReadSingleWriteLockManager lockManager;

	private ExclusiveLockManager txnLockManager;

	private boolean trackLocks = false;

	/**
	 * Flag indicating whether the Sail has been initialized.
	 */
	private boolean initialized;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new NativeStore.
	 */
	public NativeStore() {
		initialized = false;
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

	@Override
	public void setParameter(String key, String value)
	{
		if (TRIPLES_INDEXES_KEY.equals(key)) {
			setTripleIndexes(value);
		}
		else if (TRACK_LOCKS_KEY.equals(key)) {
			trackLocks = "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
		}
		else {
			super.setParameter(key, value);
		}
	}

	/**
	 * Sets the triple indexes for the native store, must be called before
	 * initialization.
	 * 
	 * @param tripleIndexes
	 *        An index strings, e.g. <tt>spoc,posc</tt>.
	 */
	public void setTripleIndexes(String tripleIndexes) {
		if (isInitialized()) {
			throw new IllegalStateException("sail has already been intialized");
		}

		this.tripleIndexes = tripleIndexes;
	}

	public String getTripleIndexes() {
		return tripleIndexes;
	}

	/**
	 * Specifiec whether updates should be synced to disk forcefully, must be
	 * called before initialization. Enabling this feature may prevent corruption
	 * in case of events like power loss, but can have a severe impact on write
	 * performance. By default, this feature is disabled.
	 */
	public void setForceSync(boolean forceSync) {
		this.forceSync = forceSync;
	}

	public boolean getForceSync() {
		return forceSync;
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
		if (isInitialized()) {
			throw new IllegalStateException("sail has already been intialized");
		}

		logger.debug("Initializing NativeStore...");

		lockManager = new MultiReadSingleWriteLockManager(trackLocks);
		txnLockManager = new ExclusiveLockManager(trackLocks);

		// Check initialization parameters
		File dataDir = getDataDir();

		if (dataDir == null) {
			throw new SailException("Data dir has not been set");
		}
		else if (!dataDir.exists()) {
			boolean success = dataDir.mkdirs();
			if (!success) {
				throw new SailException("Unable to create data directory: " + dataDir);
			}
		}
		else if (!dataDir.isDirectory()) {
			throw new SailException("The specified path does not denote a directory: " + dataDir);
		}
		else if (!dataDir.canRead()) {
			throw new SailException("Not allowed to read from the specified directory: " + dataDir);
		}

		logger.debug("Data dir is " + dataDir);

		try {
			namespaceStore = new NamespaceStore(dataDir);
			valueStore = new ValueStore(dataDir, forceSync);
			tripleStore = new TripleStore(dataDir, tripleIndexes, forceSync);
		}
		catch (IOException e) {
			throw new SailException(e);
		}

		initialized = true;
		logger.debug("NativeStore initialized");
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
		throws SailException
	{
		if (isInitialized()) {
			logger.debug("Shutting down NativeStore...");

			try {
				// Get txn lock to make sure no transaction are active
				Lock txnLock = getTxnLock();

				try {
					// Get write lock to make sure no connections are querying
					Lock writeLock = getWriteLock();
					try {
						tripleStore.close();
						valueStore.close();
						namespaceStore.close();

						initialized = false;

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

	@Override
	protected SailConnection getConnectionInternal()
		throws SailException
	{
		if (!isInitialized()) {
			throw new IllegalStateException("sail not initialized.");
		}

		try {
			return new NativeStoreConnection(this);
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	public ValueFactory getValueFactory() {
		return valueStore;
	}

	protected TripleStore getTripleStore() {
		return tripleStore;
	}

	protected ValueStore getValueStore() {
		return valueStore;
	}

	protected NamespaceStore getNamespaceStore() {
		return namespaceStore;
	}

	protected Lock getReadLock()
		throws SailException
	{
		try {
			return lockManager.getReadLock();
		}
		catch (InterruptedException e) {
			throw new SailException(e);
		}
	}

	protected Lock getWriteLock()
		throws SailException
	{
		try {
			return lockManager.getWriteLock();
		}
		catch (InterruptedException e) {
			throw new SailException(e);
		}
	}

	protected Lock getTxnLock()
		throws InterruptedException
	{
		return txnLockManager.getExclusiveLock();
	}

	protected List<Integer> getContextIDs(Resource... contexts)
		throws IOException
	{
		assert contexts.length > 0 : "contexts must not be empty";

		// Filter duplicates
		LinkedHashSet<Resource> contextSet = new LinkedHashSet<Resource>();
		Collections.addAll(contextSet, contexts);

		// Fetch IDs, filtering unknown resources from the result
		List<Integer> contextIDs = new ArrayList<Integer>(contextSet.size());
		for (Resource context : contextSet) {
			if (context == null) {
				contextIDs.add(0);
			}
			else {
				int contextID = valueStore.getID(context);
				if (contextID != NativeValue.UNKNOWN_ID) {
					contextIDs.add(contextID);
				}
			}
		}

		return contextIDs;
	}

	protected List<Integer> storeContextIDs(Resource... contexts)
		throws IOException
	{
		assert contexts.length > 0 : "contexts must not be empty";

		// Filter duplicates
		LinkedHashSet<Resource> contextSet = new LinkedHashSet<Resource>();
		Collections.addAll(contextSet, contexts);

		// Store contexts and record their IDs
		List<Integer> contextIDs = new ArrayList<Integer>(contextSet.size());
		for (Resource context : contextSet) {
			if (context == null) {
				contextIDs.add(0);
			}
			else {
				contextIDs.add(valueStore.storeValue(context));
			}
		}

		return contextIDs;
	}
}
