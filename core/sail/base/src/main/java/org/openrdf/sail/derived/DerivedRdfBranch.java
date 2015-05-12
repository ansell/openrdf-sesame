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
package org.openrdf.sail.derived;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.openrdf.IsolationLevel;
import org.openrdf.IsolationLevels;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.sail.SailException;

/**
 * An {@link RdfBranch} that keeps a delta of its state from a backing
 * {@link RdfSource}.
 * 
 * @author James Leigh
 */
public class DerivedRdfBranch implements RdfBranch {

	/**
	 * Used to prevent changes to this object's field from multiple threads.
	 */
	private final ReentrantLock semaphore = new ReentrantLock();

	/**
	 * The difference between this {@link RdfBranch} and the backing
	 * {@link RdfSource}.
	 */
	private final LinkedList<Changeset> changes = new LinkedList<Changeset>();

	/**
	 * {@link RdfSink} that have been created, but not yet
	 * {@link RdfSink#flush()}ed to this {@link RdfBranch}.
	 */
	private final Collection<Changeset> pending = new LinkedList<Changeset>();

	/**
	 * Set of open {@link RdfDataset} for this {@link RdfBranch}.
	 */
	private final Collection<RdfDataset> observers = new LinkedList<RdfDataset>();

	/**
	 * The underly {@link RdfSource} this {@link RdfBranch} is derived from.
	 */
	private final RdfSource backingSource;

	/**
	 * The {@link Model} instances that should be used to store
	 * {@link RdfSink#approve(Resource, URI, Value, Resource)} and
	 * {@link RdfSink#deprecate(Resource, URI, Value, Resource)} statements.
	 */
	private final RdfModelFactory modelFactory;

	/**
	 * If this {@link RdfBranch} should be flushed to the backing
	 * {@link RdfSource} when it is not in use.
	 */
	private final boolean autoFlush;

	/**
	 * Non-null when in {@link IsolationLevels#SNAPSHOT} (or higher) mode.
	 */
	private RdfDataset snapshot;

	/**
	 * Non-null when in {@link IsolationLevels#SERIALIZABLE} (or higher) mode.
	 */
	private RdfSink serializable;

	/**
	 * Non-null after {@link #prepare()}, but before {@link #flush()}.
	 */
	private RdfSink prepared;

	/**
	 * Creates a new in-memory {@link RdfBranch} derived from the given
	 * {@link RdfSource}.
	 * 
	 * @param backingSource
	 */
	public DerivedRdfBranch(RdfSource backingSource) {
		this(backingSource, new RdfModelFactory(), false);
	}

	/**
	 * Creates a new {@link RdfBranch} derived from the given {@link RdfSource}.
	 * 
	 * @param backingSource
	 * @param modelFactory
	 */
	public DerivedRdfBranch(RdfSource backingSource, RdfModelFactory modelFactory) {
		this(backingSource, modelFactory, false);
	}

	/**
	 * Creates a new {@link RdfBranch} derived from the given {@link RdfSource}
	 * and if <code>autoFlush</code> is true, will automatically call
	 * {@link #flush()} when not in use.
	 * 
	 * @param backingSource
	 * @param modelFactory
	 * @param autoFlush
	 */
	public DerivedRdfBranch(RdfSource backingSource, RdfModelFactory modelFactory, boolean autoFlush) {
		this.backingSource = backingSource;
		this.modelFactory = modelFactory;
		this.autoFlush = autoFlush;
	}

	@Override
	public void close()
		throws SailException
	{
		try {
			semaphore.lock();
			if (snapshot != null) {
				try {
					snapshot.close();
				}
				finally {
					snapshot = null;
				}
			}
			if (serializable != null) {
				try {
					serializable.close();
				}
				finally {
					serializable = null;
				}
			}
		}
		finally {
			semaphore.unlock();
		}
	}

	@Override
	public RdfSink sink(IsolationLevel level)
		throws SailException
	{
		Changeset changeset = new Changeset() {

			private boolean prepared;

			@Override
			public void prepare()
				throws SailException
			{
				if (!prepared) {
					preparedChangeset(this);
					prepared = true;
				}
				super.prepare();
			}

			@Override
			public void flush()
				throws SailException
			{
				merge(this);
			}

			@Override
			public void close()
				throws SailException
			{
				try {
					super.close();
				}
				finally {
					if (prepared) {
						closeChangeset(this);
						prepared = false;
					}
					autoFlush();
				}
			}

			@Override
			protected Model createEmptyModel() {
				return modelFactory.createEmptyModel();
			}
		};
		try {
			semaphore.lock();
			pending.add(changeset);
		}
		finally {
			semaphore.unlock();
		}
		return changeset;
	}

	@Override
	public RdfDataset dataset(IsolationLevel level)
		throws SailException
	{
		RdfDataset dataset = new DelegatingRdfDataset(derivedFromSerializable(level)) {

			@Override
			public void close()
				throws SailException
			{
				super.close();
				try {
					semaphore.lock();
					observers.remove(this);
					compressChanges();
					autoFlush();
				}
				finally {
					semaphore.unlock();
				}
			}
		};
		try {
			semaphore.lock();
			observers.add(dataset);
		}
		finally {
			semaphore.unlock();
		}
		return dataset;
	}

	@Override
	public RdfBranch fork() {
		return new DerivedRdfBranch(this, modelFactory);
	}

	@Override
	public void prepare()
		throws SailException
	{
		try {
			semaphore.lock();
			if (!changes.isEmpty()) {
				if (prepared == null && serializable == null) {
					prepared = backingSource.sink(IsolationLevels.NONE);
				}
				else if (prepared == null) {
					prepared = serializable;
				}
				prepare(prepared);
				prepared.prepare();
			}
		}
		finally {
			semaphore.unlock();
		}
	}

	@Override
	public void flush()
		throws SailException
	{
		try {
			semaphore.lock();
			if (!changes.isEmpty()) {
				if (prepared == null) {
					prepare();
				}
				flush(prepared);
				prepared.flush();
				try {
					if (prepared != serializable) {
						prepared.close();
					}
				}
				finally {
					prepared = null;
				}
			}
		}
		finally {
			semaphore.unlock();
		}
	}

	public boolean isChanged() {
		try {
			semaphore.lock();
			return !changes.isEmpty();
		}
		finally {
			semaphore.unlock();
		}
	}

	public String toString() {
		return backingSource.toString() + "\n" + changes.toString();
	}

	void preparedChangeset(Changeset changeset) {
		semaphore.lock();
	}

	void merge(Changeset change) {
		try {
			semaphore.lock();
			pending.remove(change);
			if (isChanged(change)) {
				Changeset merged;
				changes.add(change);
				compressChanges();
				merged = changes.getLast();
				for (Changeset c : pending) {
					c.prepend(merged);
				}
			}
		}
		finally {
			semaphore.unlock();
		}
	}

	void compressChanges() {
		try {
			semaphore.lock();
			while (changes.size() > 1 && !changes.get(changes.size() - 2).isRefback()) {
				try {
					Changeset pop = changes.removeLast();
					prepare(pop, changes.getLast());
					flush(pop, changes.getLast());
				}
				catch (SailException e) {
					// Changeset does not throw SailException
					throw new AssertionError(e);
				}
			}
		}
		finally {
			semaphore.unlock();
		}
	}

	void closeChangeset(Changeset changeset) {
		semaphore.unlock();
	}

	void autoFlush()
		throws SailException
	{
		if (autoFlush && semaphore.tryLock()) {
			try {
				if (serializable == null && observers.isEmpty()) {
					flush();
				}
			}
			finally {
				semaphore.unlock();
			}
		}
	}

	private boolean isChanged(Changeset change) {
		return change.getApproved() != null || change.getDeprecated() != null
				|| change.getApprovedContexts() != null || change.getDeprecatedContexts() != null
				|| change.getAddedNamespaces() != null || change.getRemovedPrefixes() != null
				|| change.isStatementCleared() || change.isNamespaceCleared() || change.getObservations() != null;
	}

	private RdfDataset derivedFromSerializable(IsolationLevel level)
		throws SailException
	{
		try {
			semaphore.lock();
			if (serializable == null && level.isCompatibleWith(IsolationLevels.SERIALIZABLE)) {
				serializable = backingSource.sink(level);
			}
			RdfDataset derivedFrom = derivedFromSnapshot(level);
			if (serializable == null) {
				return derivedFrom;
			}
			else {
				return new ObservingRdfDataset(derivedFrom, sink(level));
			}
		}
		finally {
			semaphore.unlock();
		}
	}

	private RdfDataset derivedFromSnapshot(IsolationLevel level)
		throws SailException
	{
		try {
			semaphore.lock();
			RdfDataset derivedFrom;
			if (this.snapshot != null) {
				// this object is already has at least snapshot isolation
				derivedFrom = new DelegatingRdfDataset(this.snapshot) {

					@Override
					public void close()
						throws SailException
					{
						// don't close snapshot yet
					}
				};
			}
			else {
				derivedFrom = backingSource.dataset(level);
				if (level.isCompatibleWith(IsolationLevels.SNAPSHOT)) {
					this.snapshot = derivedFrom;
					// don't release snapshot until this RdfSource is released
					derivedFrom = new DelegatingRdfDataset(derivedFrom) {

						@Override
						public void close()
							throws SailException
						{
							// don't close snapshot yet
						}
					};
				}
			}
			Iterator<Changeset> iter = changes.iterator();
			while (iter.hasNext()) {
				derivedFrom = new DerivedRdfDataset(derivedFrom, iter.next());
			}
			return derivedFrom;
		}
		finally {
			semaphore.unlock();
		}
	}

	private void prepare(RdfSink sink)
		throws SailException
	{
		try {
			semaphore.lock();
			Iterator<Changeset> iter = changes.iterator();
			while (iter.hasNext()) {
				prepare(iter.next(), sink);
			}
		}
		finally {
			semaphore.unlock();
		}
	}

	private void prepare(Changeset change, RdfSink sink)
		throws SailException
	{
		Set<StatementPattern> observations = change.getObservations();
		if (observations != null) {
			for (StatementPattern p : observations) {
				Resource subj = (Resource)p.getSubjectVar().getValue();
				URI pred = (URI)p.getPredicateVar().getValue();
				Value obj = p.getObjectVar().getValue();
				Var ctxVar = p.getContextVar();
				if (ctxVar == null) {
					sink.observe(subj, pred, obj);
				}
				else {
					sink.observe(subj, pred, obj, (Resource)ctxVar.getValue());
				}
			}
		}
	}

	private void flush(RdfSink sink)
		throws SailException
	{
		try {
			semaphore.lock();
			if (changes.size() == 1 && !changes.getFirst().isRefback() && sink instanceof Changeset
					&& !isChanged((Changeset)sink))
			{
				// one change to apply that is not in use to an empty Changeset
				Changeset dst = (Changeset)sink;
				dst.setChangeset(changes.pop());
			}
			else {
				Iterator<Changeset> iter = changes.iterator();
				while (iter.hasNext()) {
					flush(iter.next(), sink);
					iter.remove();
				}
			}
		}
		finally {
			semaphore.unlock();
		}
	}

	private void flush(Changeset change, RdfSink sink)
		throws SailException
	{
		prepare(change, sink);
		if (change.isNamespaceCleared()) {
			sink.clearNamespaces();
		}
		Set<String> removedPrefixes = change.getRemovedPrefixes();
		if (removedPrefixes != null) {
			for (String prefix : removedPrefixes) {
				sink.removeNamespace(prefix);
			}
		}
		Map<String, String> addedNamespaces = change.getAddedNamespaces();
		if (addedNamespaces != null) {
			for (Map.Entry<String, String> e : addedNamespaces.entrySet()) {
				sink.setNamespace(e.getKey(), e.getValue());
			}
		}
		if (change.isStatementCleared()) {
			sink.clear();
		}
		Set<Resource> deprecatedContexts = change.getDeprecatedContexts();
		if (deprecatedContexts != null && !deprecatedContexts.isEmpty()) {
			sink.clear(deprecatedContexts.toArray(new Resource[deprecatedContexts.size()]));
		}
		Model deprecated = change.getDeprecated();
		if (deprecated != null) {
			for (Statement st : deprecated) {
				sink.deprecate(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
			}
		}
		Model approved = change.getApproved();
		if (approved != null) {
			for (Statement st : approved) {
				sink.approve(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
			}
		}
	}

}
