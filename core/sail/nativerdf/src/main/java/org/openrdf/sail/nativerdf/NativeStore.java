/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import info.aduna.concurrent.locks.Lock;
import info.aduna.io.MavenUtil;

import org.openrdf.IsolationLevels;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolverClient;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolverImpl;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.derived.RdfStore;
import org.openrdf.sail.helpers.DirectoryLockManager;
import org.openrdf.sail.helpers.NotifyingSailBase;

/**
 * A SAIL implementation using B-Tree indexing on disk for storing and querying
 * its data.
 * 
 * @author Arjohn Kampman
 * @author jeen
 */
public class NativeStore extends NotifyingSailBase implements FederatedServiceResolverClient {

	/*-----------*
	 * Variables *
	 *-----------*/

	private static final String VERSION = MavenUtil.loadVersion("org.openrdf.sesame", "sesame-sail-nativerdf", "devel");

	/**
	 * Specifies which triple indexes this native store must use.
	 */
	private volatile String tripleIndexes;

	/**
	 * Flag indicating whether updates should be synced to disk forcefully. This
	 * may have a severe impact on write performance. By default, this feature is
	 * disabled.
	 */
	private volatile boolean forceSync = false;

	private volatile int valueCacheSize = ValueStore.VALUE_CACHE_SIZE;

	private volatile int valueIDCacheSize = ValueStore.VALUE_ID_CACHE_SIZE;

	private volatile int namespaceCacheSize = ValueStore.NAMESPACE_CACHE_SIZE;

	private volatile int namespaceIDCacheSize = ValueStore.NAMESPACE_ID_CACHE_SIZE;

	private RdfStore store;

	/**
	 * Data directory lock.
	 */
	private volatile Lock dirLock;

	/** independent life cycle */
	private FederatedServiceResolver serviceResolver;

	/** dependent life cycle */
	private FederatedServiceResolverImpl dependentServiceResolver;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new NativeStore.
	 */
	public NativeStore() {
		super();
		setSupportedIsolationLevels(IsolationLevels.READ_COMMITTED, IsolationLevels.SNAPSHOT_READ,
				IsolationLevels.SNAPSHOT, IsolationLevels.SERIALIZABLE);
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

	public void setValueCacheSize(int valueCacheSize) {
		this.valueCacheSize = valueCacheSize;
	}

	public void setValueIDCacheSize(int valueIDCacheSize) {
		this.valueIDCacheSize = valueIDCacheSize;
	}

	public void setNamespaceCacheSize(int namespaceCacheSize) {
		this.namespaceCacheSize = namespaceCacheSize;
	}

	public void setNamespaceIDCacheSize(int namespaceIDCacheSize) {
		this.namespaceIDCacheSize = namespaceIDCacheSize;
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
	 * @param resolver The SERVICE reslover to set.
	 */
	public synchronized void setFederatedServiceResolver(FederatedServiceResolver resolver) {
		this.serviceResolver = resolver;
	}

	/**
	 * Initializes this NativeStore.
	 * 
	 * @exception SailException
	 *            If this NativeStore could not be initialized using the
	 *            parameters that have been set.
	 */
	@Override
	protected void initializeInternal()
		throws SailException
	{
		logger.debug("Initializing NativeStore...");

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

		// try to lock the directory or fail
		dirLock = new DirectoryLockManager(dataDir).lockOrFail();

		logger.debug("Data dir is " + dataDir);

		try {
			File versionFile = new File(dataDir, "nativerdf.ver");
			String version = versionFile.exists() ? FileUtils.readFileToString(versionFile) : null;
			if (!VERSION.equals(version) && upgradeStore(dataDir, version)) {
				FileUtils.writeStringToFile(versionFile, VERSION);
			}
			store = new NativeRdfStore(dataDir, forceSync, valueCacheSize, valueIDCacheSize, namespaceCacheSize,
					namespaceIDCacheSize, tripleIndexes);
		}
		catch (Throwable e) {
			// NativeStore initialization failed, release any allocated files
			dirLock.release();

			throw new SailException(e);
		}

		logger.debug("NativeStore initialized");
	}

	@Override
	protected void shutDownInternal()
		throws SailException
	{
		logger.debug("Shutting down NativeStore...");

		try {
			store.close();

			logger.debug("NativeStore shut down");
		}
		finally {
			dirLock.release();
			if (dependentServiceResolver != null) {
				dependentServiceResolver.shutDown();
			}
			logger.debug("NativeStore shut down");
		}
	}

	public boolean isWritable() {
		return getDataDir().canWrite();
	}

	@Override
	protected NotifyingSailConnection getConnectionInternal()
		throws SailException
	{
		try {
			return new NativeStoreConnection(this);
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	public ValueFactory getValueFactory() {
		return store.getValueFactory();
	}

	RdfStore getRdfStore() {
		return store;
	}

	private boolean upgradeStore(File dataDir, String version)
		throws IOException, SailException
	{
		if (version == null) {
			// either a new store or a pre-2.8.2 store
			ValueStore valueStore = new ValueStore(dataDir);
			try {
				valueStore.checkConsistency();
				return true; // good enough
			}
			finally {
				valueStore.close();
			}
		}
		else {
			return false; // no upgrade needed
		}
	}
}
