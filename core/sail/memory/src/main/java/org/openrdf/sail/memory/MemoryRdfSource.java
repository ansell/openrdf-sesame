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
package org.openrdf.sail.memory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.concurrent.locks.ExclusiveLockManager;
import info.aduna.concurrent.locks.Lock;
import info.aduna.concurrent.locks.LockingIteration;
import info.aduna.concurrent.locks.ReadPrefReadWriteLockManager;
import info.aduna.concurrent.locks.ReadWriteLockManager;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;
import info.aduna.iteration.EmptyIteration;

import org.openrdf.IsolationLevel;
import org.openrdf.IsolationLevels;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailConflictException;
import org.openrdf.sail.SailException;
import org.openrdf.sail.derived.DerivedRDfSource;
import org.openrdf.sail.derived.RdfDataset;
import org.openrdf.sail.derived.RdfSink;
import org.openrdf.sail.derived.RdfSource;
import org.openrdf.sail.memory.model.MemResource;
import org.openrdf.sail.memory.model.MemStatement;
import org.openrdf.sail.memory.model.MemStatementIterator;
import org.openrdf.sail.memory.model.MemStatementList;
import org.openrdf.sail.memory.model.MemURI;
import org.openrdf.sail.memory.model.MemValue;
import org.openrdf.sail.memory.model.MemValueFactory;

/**
 * @author James Leigh
 */
class MemoryRdfSource implements RdfSource {

	private final Logger logger = LoggerFactory.getLogger(MemoryRdfSource.class);

	/**
	 * Factory/cache for MemValue objects.
	 */
	private final MemValueFactory valueFactory = new MemValueFactory();

	/**
	 * List containing all available statements.
	 */
	private final MemStatementList statements = new MemStatementList(256);

	volatile int currentSnapshot;

	/**
	 * Store for namespace prefix info.
	 */
	private final MemNamespaceStore namespaceStore = new MemNamespaceStore();

	/**
	 * Lock manager used to give the snapshot cleanup thread exclusive access to
	 * the statement list.
	 */
	private final ReadWriteLockManager statementListLockManager;

	/**
	 * Lock manager used to prevent concurrent writes.
	 */
	private final ExclusiveLockManager txnLockManager;

	/**
	 * Cleanup thread that removes deprecated statements when no other threads
	 * are accessing this list. Seee {@link #scheduleSnapshotCleanup()}.
	 */
	private volatile Thread snapshotCleanupThread;

	/**
	 * Semaphore used to synchronize concurrent access to
	 * {@link #snapshotCleanupThread}.
	 */
	private final Object snapshotCleanupThreadSemaphore = new Object();

	private boolean released;

	public MemoryRdfSource(boolean debug) {
		statementListLockManager = new ReadPrefReadWriteLockManager(debug);
		txnLockManager = new ExclusiveLockManager(debug);
	}

	public MemValueFactory getValueFactory() {
		return valueFactory;
	}

	@Override
	public boolean isActive() {
		return !released;
	}

	@Override
	public void release() {
		released = true;
		valueFactory.clear();
		statements.clear();
	}

	@Override
	public RdfSource fork()
		throws SailException
	{
		return new DerivedRDfSource(this);
	}

	@Override
	public void prepare() {
		// assume all transactions will reasonably commit
	}

	@Override
	public void flush() {
		scheduleSnapshotCleanup();
	}

	@Override
	public RdfSink sink(IsolationLevel level)
		throws SailException
	{
		return new MemoryRdfSink(level.isCompatibleWith(IsolationLevels.SERIALIZABLE));
	}

	@Override
	public MemoryRdfDataset snapshot(IsolationLevel level)
		throws SailException
	{
		if (level.isCompatibleWith(IsolationLevels.SNAPSHOT_READ)) {
			return new MemoryRdfDataset(currentSnapshot);
		} else {
			return new MemoryRdfDataset();
		}
	}

	Lock openStatementsWriteLock()
		throws SailException
	{
		try {
			return statementListLockManager.getWriteLock();
		}
		catch (InterruptedException e) {
			throw new SailException(e);
		}
	}

	Lock openStatementsReadLock()
		throws SailException
	{
		try {
			return statementListLockManager.getReadLock();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new SailException(e);
		}
	}

	Lock openTransactionLock()
		throws SailException
	{
		try {
			return txnLockManager.getExclusiveLock();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new SailException(e);
		}
	}

	/**
	 * Creates a StatementIterator that contains the statements matching the
	 * specified pattern of subject, predicate, object, context. Inferred
	 * statements are excluded when <tt>explicitOnly</tt> is set to <tt>true</tt>
	 * . Statements from the null context are excluded when
	 * <tt>namedContextsOnly</tt> is set to <tt>true</tt>. The returned
	 * StatementIterator will assume the specified read mode.
	 */
	CloseableIteration<MemStatement, SailException> createStatementIterator(Resource subj, URI pred,
			Value obj, boolean explicitOnly, int snapshot, Resource... contexts)
	{
		// Perform look-ups for value-equivalents of the specified values
		MemResource memSubj = valueFactory.getMemResource(subj);
		if (subj != null && memSubj == null) {
			// non-existent subject
			return new EmptyIteration<MemStatement, SailException>();
		}

		MemURI memPred = valueFactory.getMemURI(pred);
		if (pred != null && memPred == null) {
			// non-existent predicate
			return new EmptyIteration<MemStatement, SailException>();
		}

		MemValue memObj = valueFactory.getMemValue(obj);
		if (obj != null && memObj == null) {
			// non-existent object
			return new EmptyIteration<MemStatement, SailException>();
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
				return new EmptyIteration<MemStatement, SailException>();
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
				return new EmptyIteration<MemStatement, SailException>();
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

		return new MemStatementIterator<SailException>(smallestList, memSubj, memPred, memObj, explicitOnly,
				snapshot, memContexts);
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
		// System.out.println("cleanSnapshots() starting...");
		// long startTime = System.currentTimeMillis();
		MemStatementList statements = this.statements;
	
		if (statements == null) {
			// Store has been shut down
			return;
		}
	
		// Sets used to keep track of which lists have already been processed
		HashSet<MemValue> processedSubjects = new HashSet<MemValue>();
		HashSet<MemValue> processedPredicates = new HashSet<MemValue>();
		HashSet<MemValue> processedObjects = new HashSet<MemValue>();
		HashSet<MemValue> processedContexts = new HashSet<MemValue>();
	
		Lock stLock = statementListLockManager.getWriteLock();
		try {
			for (int i = statements.size() - 1; i >= 0; i--) {
				MemStatement st = statements.get(i);
	
				if (st.getTillSnapshot() <= currentSnapshot) {
					MemResource subj = st.getSubject();
					if (processedSubjects.add(subj)) {
						subj.cleanSnapshotsFromSubjectStatements(currentSnapshot);
					}
	
					MemURI pred = st.getPredicate();
					if (processedPredicates.add(pred)) {
						pred.cleanSnapshotsFromPredicateStatements(currentSnapshot);
					}
	
					MemValue obj = st.getObject();
					if (processedObjects.add(obj)) {
						obj.cleanSnapshotsFromObjectStatements(currentSnapshot);
					}
	
					MemResource context = st.getContext();
					if (context != null && processedContexts.add(context)) {
						context.cleanSnapshotsFromContextStatements(currentSnapshot);
					}
	
					// stale statement
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
	
		// long endTime = System.currentTimeMillis();
		// System.out.println("cleanSnapshots() took " + (endTime - startTime) +
		// " ms");
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

	private final class MemoryRdfSink implements RdfSink {
		private final int serializable;
		private final Lock txnStLock;
		private int nextSnapshot;
		private boolean conflict;
		private boolean released;
		private Lock txnLock;

		public MemoryRdfSink(boolean serializable) throws SailException {
			if (serializable) {
				this.serializable = currentSnapshot;
			} else {
				this.serializable = Integer.MAX_VALUE;
			}
			txnStLock = openStatementsReadLock();
		}

		public boolean isActive() {
			return !released;
		}

		public void release() {
			released = true;
			if (txnLock != null) {
				if (!conflict) {
					currentSnapshot = nextSnapshot;
				}
				txnLock.release();
			}
			if (txnStLock != null) {
				txnStLock.release();
			}
		}

		@Override
		public synchronized void setNamespace(String prefix, String name) throws SailException {
			acquireExclusiveTransactionLock();
			namespaceStore.setNamespace(prefix, name);
		}

		@Override
		public void removeNamespace(String prefix) throws SailException {
			acquireExclusiveTransactionLock();
			namespaceStore.removeNamespace(prefix);
		}

		@Override
		public void clearNamespaces() throws SailException {
			acquireExclusiveTransactionLock();
			namespaceStore.clear();
		}

		@Override
		public void observe(Resource subj, URI pred, Value obj, Resource... contexts)
			throws SailException
		{
			acquireExclusiveTransactionLock();
			CloseableIteration<MemStatement, SailException> iter;
			iter = createStatementIterator(subj, pred, obj, true, -1, contexts);
			try {
				while (iter.hasNext()) {
					MemStatement st = iter.next();
					int since = st.getSinceSnapshot();
					int till = st.getTillSnapshot();
					if (serializable < since && since < nextSnapshot || serializable < till && till < nextSnapshot)
					{
						conflict = true;
						throw new SailConflictException("Observed State has Changed");
					}
				}
			}
			finally {
				iter.close();
			}
		}

		@Override
		public void clear(Resource... contexts)
			throws SailException
		{
			acquireExclusiveTransactionLock();
			CloseableIteration<MemStatement, SailException> iter;
			iter = createStatementIterator(null, null, null, true, nextSnapshot, contexts);
			try {
				while (iter.hasNext()) {
					MemStatement st = iter.next();
					st.setTillSnapshot(nextSnapshot);
				}
			}
			finally {
				iter.close();
			}
		}

		@Override
		public synchronized void addExplicit(Resource subj, URI pred, Value obj, Resource ctx)
			throws SailException
		{
			acquireExclusiveTransactionLock();
			addStatement(subj, pred, obj, ctx, true);
		}

		@Override
		public synchronized void removeExplicit(Resource subj, URI pred, Value obj, Resource ctx)
			throws SailException
		{
			acquireExclusiveTransactionLock();
			CloseableIteration<MemStatement, SailException> iter;
			iter = createStatementIterator(subj, pred, obj, true, nextSnapshot, ctx);
			try {
				while (iter.hasNext()) {
					MemStatement st = iter.next();
					st.setTillSnapshot(nextSnapshot);
				}
			}
			finally {
				iter.close();
			}
		}

		@Override
		public synchronized void addInferred(Resource subj, URI pred, Value obj, Resource ctx)
			throws SailException
		{
			acquireExclusiveTransactionLock();
			addStatement(subj, pred, obj, ctx, false);
		}

		@Override
		public synchronized void removeInferred(Resource subj, URI pred, Value obj, Resource ctx)
			throws SailException
		{
			acquireExclusiveTransactionLock();
			CloseableIteration<MemStatement, SailException> iter;
			iter = createStatementIterator(subj, pred, obj, false, nextSnapshot, ctx);
			try {
				while (iter.hasNext()) {
					MemStatement st = iter.next();
					st.setTillSnapshot(nextSnapshot);
				}
			}
			finally {
				iter.close();
			}
		}

		private void acquireExclusiveTransactionLock()
			throws SailException
		{
			if (txnLock == null) {
				boolean releaseLocks = true;
				txnLock = openTransactionLock();
				try {
					nextSnapshot = currentSnapshot + 1;
					releaseLocks = false;
				}
				finally {
					if (releaseLocks) {
						txnLock.release();
					}
				}
			}
		}

		private MemStatement addStatement(Resource subj, URI pred, Value obj, Resource context, boolean explicit)
			throws SailException
		{
			// Get or create MemValues for the operands
			MemResource memSubj = valueFactory.getOrCreateMemResource(subj);
			MemURI memPred = valueFactory.getOrCreateMemURI(pred);
			MemValue memObj = valueFactory.getOrCreateMemValue(obj);
			MemResource memContext = (context == null) ? null : valueFactory.getOrCreateMemResource(context);

			if (memSubj.hasStatements() && memPred.hasStatements() && memObj.hasStatements()
					&& (memContext == null || memContext.hasStatements()))
			{
				// All values are used in at least one statement. Possibly, the
				// statement is already present. Check this.
				CloseableIteration<MemStatement, SailException> stIter = createStatementIterator(
						memSubj, memPred, memObj, false, Integer.MAX_VALUE - 1, memContext);

				try {
					if (stIter.hasNext()) {
						// statement is already present, update its transaction
						// status if appropriate
						MemStatement st = stIter.next();

						if (!st.isExplicit() && explicit) {
							// Implicit statement is now added explicitly
							st.setTillSnapshot(nextSnapshot);
						} else if (!st.isInSnapshot(nextSnapshot)) {
							st.setSinceSnapshot(nextSnapshot);
						} else {
							// statement already exists
							return null;
						}
					}
				}
				finally {
					stIter.close();
				}
			}

			// completely new statement
			MemStatement st = new MemStatement(memSubj, memPred, memObj, memContext, explicit, nextSnapshot);
			statements.add(st);
			st.addToComponentLists();
			return st;
		}
	}

	/**
	 * @author James Leigh
	 */
	private final class MemoryRdfDataset implements RdfDataset {

		private final int snapshot;

		private final Lock lock;

		private boolean released;

		public MemoryRdfDataset()
			throws SailException
		{
			this.snapshot = -1;
			this.lock = null;
		}

		public MemoryRdfDataset(int snapshot)
			throws SailException
		{
			this.snapshot = snapshot;
			this.lock = openStatementsReadLock();
		}

		@Override
		public boolean isActive() {
			return !released;
		}

		@Override
		public void release() {
			released = true;
			if (lock != null) {
				// serializable read or higher isolation
				lock.release();
			}
		}

		@Override
		public String getNamespace(String prefix)
			throws SailException
		{
			return namespaceStore.getNamespace(prefix);
		}

		@Override
		public CloseableIteration<? extends Namespace, SailException> getNamespaces() {
			return new CloseableIteratorIteration<Namespace, SailException>(namespaceStore.iterator());
		}

		@Override
		public CloseableIteration<? extends Resource, SailException> getContextIDs()
			throws SailException
		{
			// Note: we can't do this in a streaming fashion due to concurrency
			// issues; iterating over the set of URIs or bnodes while another
			// thread
			// adds statements with new resources would result in
			// ConcurrentModificationException's (issue SES-544).

			// Create a list of all resources that are used as contexts
			ArrayList<MemResource> contextIDs = new ArrayList<MemResource>(32);

			Lock stLock = openStatementsReadLock();

			try {
				synchronized (valueFactory) {
					int snapshot = getCurrentSnapshot();
					for (MemResource memResource : valueFactory.getMemURIs()) {
						if (isContextResource(memResource, snapshot)) {
							contextIDs.add(memResource);
						}
					}

					for (MemResource memResource : valueFactory.getMemBNodes()) {
						if (isContextResource(memResource, snapshot)) {
							contextIDs.add(memResource);
						}
					}
				}
			}
			finally {
				stLock.release();
			}

			return new CloseableIteratorIteration<MemResource, SailException>(contextIDs.iterator());
		}

		@Override
		public CloseableIteration<MemStatement, SailException> getStatements(Resource subj, URI pred,
				Value obj, Resource... contexts)
			throws SailException
		{
			boolean releaseLock = true;
			Lock stLock = openStatementsReadLock();
			try {
				LockingIteration<MemStatement, SailException> ret = new LockingIteration<MemStatement, SailException>(
						stLock, createStatementIterator(subj, pred, obj, false, getCurrentSnapshot(), contexts));
				releaseLock = false;
				return ret;
			}
			finally {
				if (releaseLock) {
					stLock.release();
				}
			}
		}

		@Override
		public CloseableIteration<MemStatement, SailException> getInferred(Resource subj, URI pred, Value obj,
				Resource... contexts)
			throws SailException
		{
			final CloseableIteration<MemStatement, SailException> iter = getStatements(subj, pred, obj, contexts);
			return new CloseableIteration<MemStatement, SailException>() {
		
				private MemStatement next;
		
				@Override
				public boolean hasNext()
					throws SailException
				{
					while (next == null && iter.hasNext()) {
						next = iter.next();
						if (next.isExplicit()) {
							next = null;
						}
					}
					return next != null;
				}
		
				@Override
				public MemStatement next()
					throws SailException
				{
					try {
						if (hasNext()) {
							return next;
						}
						else {
							throw new NoSuchElementException();
						}
					}
					finally {
						next = null;
					}
				}
		
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
		
				@Override
				public void close()
					throws SailException
				{
					iter.close();
				}
			};
		}

		@Override
		public CloseableIteration<MemStatement, SailException> getExplicit(Resource subj, URI pred, Value obj,
				Resource... contexts)
			throws SailException
		{
			final CloseableIteration<MemStatement, SailException> iter = getStatements(subj, pred, obj, contexts);
			return new CloseableIteration<MemStatement, SailException>() {
		
				private MemStatement next;
		
				@Override
				public boolean hasNext()
					throws SailException
				{
					while (next == null && iter.hasNext()) {
						next = iter.next();
						if (!next.isExplicit()) {
							next = null;
						}
					}
					return next != null;
				}
		
				@Override
				public MemStatement next()
					throws SailException
				{
					try {
						if (hasNext()) {
							return next;
						}
						else {
							throw new NoSuchElementException();
						}
					}
					finally {
						next = null;
					}
				}
		
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
		
				@Override
				public void close()
					throws SailException
				{
					iter.close();
				}
			};
		}

		private int getCurrentSnapshot() {
			if (snapshot >= 0) {
				return snapshot;
			} else {
				return currentSnapshot;
			}
		}

		private boolean isContextResource(MemResource memResource, int snapshot)
			throws SailException
		{
			MemStatementList contextStatements = memResource.getContextStatementList();

			// Filter resources that are not used as context identifier
			if (contextStatements.size() == 0) {
				return false;
			}

			// Filter more thoroughly by considering snapshot and read-mode
			// parameters
			MemStatementIterator<SailException> iter = new MemStatementIterator<SailException>(
					contextStatements, null, null, null, false, snapshot);
			try {
				return iter.hasNext();
			}
			finally {
				iter.close();
			}
		}
	}
}
