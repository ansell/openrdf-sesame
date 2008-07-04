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
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;
import info.aduna.iteration.DistinctIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.FilterIteration;
import info.aduna.iteration.UnionIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.sail.nativerdf.btree.RecordIterator;
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

		storeLockManager = new WritePrefReadWriteLockManager(trackLocks);
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
	}

	public boolean isWritable() {
		return getDataDir().canWrite();
	}

	@Override
	protected NotifyingSailConnection getConnectionInternal()
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
			return storeLockManager.getReadLock();
		}
		catch (InterruptedException e) {
			throw new SailException(e);
		}
	}

	protected Lock getWriteLock()
		throws SailException
	{
		try {
			return storeLockManager.getWriteLock();
		}
		catch (InterruptedException e) {
			throw new SailException(e);
		}
	}

	protected Lock getTransactionLock()
		throws SailException
	{
		try {
			return txnLockManager.getExclusiveLock();
		}
		catch (InterruptedException e) {
			throw new SailException(e);
		}
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

	protected CloseableIteration<Resource, IOException> getContextIDs(boolean readTransaction)
		throws IOException
	{
		CloseableIteration<? extends Statement, IOException> stIter;
		CloseableIteration<Resource, IOException> ctxIter;
		RecordIterator btreeIter;
		btreeIter = tripleStore.getAllTriplesSortedByContext(readTransaction);
		if (btreeIter == null) {
			// Iterator over all statements
			stIter = createStatementIterator(null, null, null, true,
					readTransaction);
		} else {
			stIter = new NativeStatementIterator(btreeIter, valueStore);
		}
		// Filter statements without context resource
		stIter = new FilterIteration<Statement, IOException>(stIter) {
			@Override
			protected boolean accept(Statement st) {
				return st.getContext() != null;
			}
		};
		// Return the contexts of the statements
		ctxIter = new ConvertingIteration<Statement, Resource, IOException>(
				stIter) {
			@Override
			protected Resource convert(Statement st) {
				return st.getContext();
			}
		};
		if (btreeIter == null) {
			// Filtering any duplicates
			ctxIter = new DistinctIteration<Resource, IOException>(ctxIter);
		} else {
			// Filtering sorted duplicates
			ctxIter = new FilterIteration<Resource, IOException>(ctxIter) {
				private Resource last = null;

				@Override
				protected boolean accept(Resource ctx) throws IOException {
					boolean equal = ctx.equals(last);
					last = ctx;
					return !equal;
				}
			};
		}
		return ctxIter;
	}

	/**
	 * Creates a statement iterator based on the supplied pattern.
	 * 
	 * @param subj
	 *        The subject of the pattern, or <tt>null</tt> to indicate a
	 *        wildcard.
	 * @param pred
	 *        The predicate of the pattern, or <tt>null</tt> to indicate a
	 *        wildcard.
	 * @param obj
	 *        The object of the pattern, or <tt>null</tt> to indicate a
	 *        wildcard.
	 * @param context
	 *        The context of the pattern, or <tt>null</tt> to indicate a
	 *        wildcard
	 * @return A StatementIterator that can be used to iterate over the
	 *         statements that match the specified pattern.
	 */
	protected CloseableIteration<? extends Statement, IOException> createStatementIterator(Resource subj,
			URI pred, Value obj, boolean includeInferred, boolean readTransaction, Resource... contexts)
		throws IOException
	{
		int subjID = NativeValue.UNKNOWN_ID;
		if (subj != null) {
			subjID = valueStore.getID(subj);
			if (subjID == NativeValue.UNKNOWN_ID) {
				return new EmptyIteration<Statement, IOException>();
			}
		}

		int predID = NativeValue.UNKNOWN_ID;
		if (pred != null) {
			predID = valueStore.getID(pred);
			if (predID == NativeValue.UNKNOWN_ID) {
				return new EmptyIteration<Statement, IOException>();
			}
		}

		int objID = NativeValue.UNKNOWN_ID;
		if (obj != null) {
			objID = valueStore.getID(obj);
			if (objID == NativeValue.UNKNOWN_ID) {
				return new EmptyIteration<Statement, IOException>();
			}
		}

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

		ArrayList<NativeStatementIterator> perContextIterList = new ArrayList<NativeStatementIterator>(
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

			perContextIterList.add(new NativeStatementIterator(btreeIter, valueStore));
		}

		return new UnionIteration<Statement, IOException>(perContextIterList);
	}
}
