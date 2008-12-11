/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
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
import info.aduna.concurrent.locks.ReadWriteLockManager;
import info.aduna.concurrent.locks.WritePrefReadWriteLockManager;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Cursor;
import org.openrdf.query.algebra.evaluation.cursors.DistinctCursor;
import org.openrdf.query.algebra.evaluation.cursors.NamedContextCursor;
import org.openrdf.query.algebra.evaluation.cursors.ReducedCursor;
import org.openrdf.query.algebra.evaluation.cursors.UnionCursor;
import org.openrdf.query.base.ConvertingCursor;
import org.openrdf.query.impl.EmptyCursor;
import org.openrdf.sail.SailMetaData;
import org.openrdf.sail.helpers.DirectoryLockManager;
import org.openrdf.sail.helpers.SailUtil;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.helpers.InferencerSailBase;
import org.openrdf.sail.inferencer.helpers.SynchronizedInferencerConnection;
import org.openrdf.sail.nativerdf.btree.RecordIterator;
import org.openrdf.sail.nativerdf.model.NativeValue;
import org.openrdf.store.StoreException;

/**
 * A SAIL implementation using B-Tree indexing on disk for storing and querying
 * its data.
 * 
 * @author Arjohn Kampman
 * @author jeen
 */
public class NativeStore extends InferencerSailBase {

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
	 * Lock manager used to synchronize read and write access to the store.
	 */
	private ReadWriteLockManager storeLockManager;

	/**
	 * Lock manager used to prevent concurrent transactions.
	 */
	private ExclusiveLockManager txnLockManager;

	/**
	 * Flag indicating whether the Sail has been initialized.
	 */
	private boolean initialized;

	/**
	 * Data directory lock.
	 */
	private Lock dirLock;

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
	public SailMetaData getMetaData() {
		return new NativeStoreMetaData(this);
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
	 * @exception StoreException
	 *            If this RdfRepository could not be initialized using the
	 *            parameters that have been set.
	 */
	public void initialize()
		throws StoreException
	{
		if (isInitialized()) {
			throw new IllegalStateException("sail has already been intialized");
		}

		logger.debug("Initializing NativeStore...");

		storeLockManager = new WritePrefReadWriteLockManager(SailUtil.isDebugEnabled());
		txnLockManager = new ExclusiveLockManager(SailUtil.isDebugEnabled());

		// Check initialization parameters
		File dataDir = getDataDir();

		if (dataDir == null) {
			throw new StoreException("Data dir has not been set");
		}
		else if (!dataDir.exists()) {
			boolean success = dataDir.mkdirs();
			if (!success) {
				throw new StoreException("Unable to create data directory: " + dataDir);
			}
		}
		else if (!dataDir.isDirectory()) {
			throw new StoreException("The specified path does not denote a directory: " + dataDir);
		}
		else if (!dataDir.canRead()) {
			throw new StoreException("Not allowed to read from the specified directory: " + dataDir);
		}

		// try to lock the directory or fail
		dirLock = new DirectoryLockManager(dataDir).lockOrFail();

		logger.debug("Data dir is " + dataDir);

		try {
			namespaceStore = new NamespaceStore(dataDir);
			valueStore = new ValueStore(dataDir, forceSync);
			tripleStore = new TripleStore(dataDir, tripleIndexes, forceSync);
		}
		catch (IOException e) {
			throw new StoreException(e);
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
		throws StoreException
	{
		if (isInitialized()) {
			logger.debug("Shutting down NativeStore...");

			Lock txnLock = getTransactionLock();
			try {
				Lock writeLock = getWriteLock();
				try {
					tripleStore.close();
					valueStore.close();
					namespaceStore.close();

					initialized = false;

					logger.debug("NativeStore shut down");
				}
				catch (IOException e) {
					throw new StoreException(e);
				}
				finally {
					writeLock.release();
				}
			}
			finally {
				txnLock.release();
				dirLock.release();
			}
		}
	}

	public boolean isWritable() {
		return getDataDir().canWrite();
	}

	@Override
	protected InferencerConnection getConnectionInternal()
		throws StoreException
	{
		if (!isInitialized()) {
			throw new IllegalStateException("sail not initialized.");
		}

		try {
			InferencerConnection con = new NativeStoreConnection(this);
			return new SynchronizedInferencerConnection(con);
		}
		catch (IOException e) {
			throw new StoreException(e);
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
		throws StoreException
	{
		try {
			return storeLockManager.getReadLock();
		}
		catch (InterruptedException e) {
			throw new StoreException(e);
		}
	}

	protected Lock getWriteLock()
		throws StoreException
	{
		try {
			return storeLockManager.getWriteLock();
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

	protected List<Integer> getContextIDs(Resource... contexts)
		throws IOException
	{
		assert contexts == null || contexts.length > 0 : "contexts must not be empty";

		// Filter duplicates
		LinkedHashSet<Resource> contextSet = new LinkedHashSet<Resource>();
		Collections.addAll(contextSet, OpenRDFUtil.notNull(contexts));

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

	protected Cursor<Resource> getContextIDs(boolean readTransaction)
		throws IOException
	{
		Cursor<? extends Statement> stIter;
		Cursor<Resource> ctxIter;
		RecordIterator btreeIter;
		btreeIter = tripleStore.getAllTriplesSortedByContext(readTransaction);
		if (btreeIter == null) {
			// Iterator over all statements
			stIter = createStatementCursor(null, null, null, true, readTransaction);
		}
		else {
			stIter = new NativeStatementCursor(btreeIter, valueStore);
		}
		// Filter statements without context resource
		stIter = new NamedContextCursor(stIter);
		// Return the contexts of the statements
		ctxIter = new ConvertingCursor<Statement, Resource>(stIter) {

			@Override
			protected Resource convert(Statement st) {
				return st.getContext();
			}

			@Override
			protected String getName() {
				return "Context";
			}
		};
		if (btreeIter == null) {
			// Filtering any duplicates
			ctxIter = new DistinctCursor<Resource>(ctxIter);
		}
		else {
			// Filtering sorted duplicates
			ctxIter = new ReducedCursor<Resource>(ctxIter);
		}
		return ctxIter;
	}

	/**
	 * Creates a statement cursor based on the supplied pattern.
	 * 
	 * @param subj
	 *        The subject of the pattern, or <tt>null</tt> to indicate a
	 *        wildcard.
	 * @param pred
	 *        The predicate of the pattern, or <tt>null</tt> to indicate a
	 *        wildcard.
	 * @param obj
	 *        The object of the pattern, or <tt>null</tt> to indicate a wildcard.
	 * @param context
	 *        The context of the pattern, or <tt>null</tt> to indicate a wildcard
	 * @return A Cursor that can be used to iterate over the statements that
	 *         match the specified pattern.
	 */
	protected Cursor<? extends Statement> createStatementCursor(Resource subj, URI pred, Value obj,
			boolean includeInferred, boolean readTransaction, Resource... contexts)
		throws IOException
	{
		int subjID = NativeValue.UNKNOWN_ID;
		if (subj != null) {
			subjID = valueStore.getID(subj);
			if (subjID == NativeValue.UNKNOWN_ID) {
				return new EmptyCursor<Statement>();
			}
		}

		int predID = NativeValue.UNKNOWN_ID;
		if (pred != null) {
			predID = valueStore.getID(pred);
			if (predID == NativeValue.UNKNOWN_ID) {
				return new EmptyCursor<Statement>();
			}
		}

		int objID = NativeValue.UNKNOWN_ID;
		if (obj != null) {
			objID = valueStore.getID(obj);
			if (objID == NativeValue.UNKNOWN_ID) {
				return new EmptyCursor<Statement>();
			}
		}

		contexts = OpenRDFUtil.notNull(contexts);
		List<Integer> contextIDList = new ArrayList<Integer>(contexts.length);
		if (contexts.length == 0) {
			contextIDList.add(NativeValue.UNKNOWN_ID);
		}
		else {
			for (Resource context : contexts) {
				if (context == null) {
					contextIDList.add(0);
				}
				else {
					int contextID = valueStore.getID(context);

					if (contextID != NativeValue.UNKNOWN_ID) {
						contextIDList.add(contextID);
					}
				}
			}
		}

		ArrayList<NativeStatementCursor> perContextIterList = new ArrayList<NativeStatementCursor>(
				contextIDList.size());

		for (int contextID : contextIDList) {
			RecordIterator btreeIter;

			if (includeInferred) {
				// Get both explicit and inferred statements
				btreeIter = tripleStore.getTriples(subjID, predID, objID, contextID, readTransaction);
			}
			else {
				// Only get explicit statements
				btreeIter = tripleStore.getTriples(subjID, predID, objID, contextID, true, readTransaction);
			}

			perContextIterList.add(new NativeStatementCursor(btreeIter, valueStore));
		}

		if (perContextIterList.size() == 1) {
			return perContextIterList.get(0);
		}
		else if (perContextIterList.isEmpty()) {
			return EmptyCursor.emptyCursor();
		}
		else {
			return new UnionCursor<Statement>(perContextIterList);
		}
	}

	protected double cardinality(Resource subj, URI pred, Value obj, Resource context)
		throws IOException
	{
		int subjID = NativeValue.UNKNOWN_ID;
		if (subj != null) {
			subjID = valueStore.getID(subj);
			if (subjID == NativeValue.UNKNOWN_ID) {
				return 0;
			}
		}

		int predID = NativeValue.UNKNOWN_ID;
		if (pred != null) {
			predID = valueStore.getID(pred);
			if (predID == NativeValue.UNKNOWN_ID) {
				return 0;
			}
		}

		int objID = NativeValue.UNKNOWN_ID;
		if (obj != null) {
			objID = valueStore.getID(obj);
			if (objID == NativeValue.UNKNOWN_ID) {
				return 0;
			}
		}

		int contextID = NativeValue.UNKNOWN_ID;
		if (context != null) {
			contextID = valueStore.getID(context);
			if (contextID == NativeValue.UNKNOWN_ID) {
				return 0;
			}
		}

		return tripleStore.cardinality(subjID, predID, objID, contextID);
	}
}
