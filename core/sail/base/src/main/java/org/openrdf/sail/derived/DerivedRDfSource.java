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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.openrdf.IsolationLevel;
import org.openrdf.IsolationLevels;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.sail.SailException;

/**
 * @author James Leigh
 */
public class DerivedRDfSource implements RdfSource {

	private final ReentrantLock semaphore = new ReentrantLock();

	private final LinkedList<Changeset> changes = new LinkedList<Changeset>();

	private final LinkedList<Changeset> pending = new LinkedList<Changeset>();

	private final LinkedList<RdfDataset> observers = new LinkedList<RdfDataset>();

	private final RdfSource backingSource;

	private final boolean autoFlush;

	/**
	 * Non-null when in {@link IsolationLevels#SNAPSHOT} (or higher) mode.
	 */
	private RdfDataset snapshot;

	/**
	 * Non-null when in {@link IsolationLevels#SERIALIZABLE} (or higher) mode.
	 */
	private RdfSink serializable;

	public DerivedRDfSource(RdfSource backingSource) {
		this(backingSource, false);
	}

	public DerivedRDfSource(RdfSource backingSource, boolean autoFlush) {
		this.backingSource = backingSource;
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
	public RdfDataset snapshot(IsolationLevel level)
		throws SailException
	{
		RdfDataset dataset = new DelegatingRdfDataset(derivedFromSerializable(level), true) {

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
	public RdfSource fork() {
		return new DerivedRDfSource(this);
	}

	@Override
	public synchronized void prepare()
		throws SailException
	{
		try {
			semaphore.lock();
			if (serializable == null) {
				serializable = backingSource.sink(IsolationLevels.NONE);
			}
			prepare(serializable);
			serializable.prepare();
		}
		finally {
			semaphore.unlock();
		}
	}

	@Override
	public synchronized void flush()
		throws SailException
	{
		try {
			semaphore.lock();
			if (serializable == null) {
				prepare();
			}
			flush(serializable);
			serializable.flush();
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
				if (!changes.isEmpty() && observers.isEmpty()) {
					boolean autoClose = serializable == null;
					try {
						flush();
					} finally {
						if (autoClose) {
							try {
								serializable.close();
							}
							finally {
								serializable = null;
							}
						}
					}
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
				derivedFrom = new DelegatingRdfDataset(this.snapshot, false);
			}
			else {
				derivedFrom = backingSource.snapshot(level);
				if (level.isCompatibleWith(IsolationLevels.SNAPSHOT)) {
					this.snapshot = derivedFrom;
					// don't release snapshot until this RdfSource is released
					derivedFrom = new DelegatingRdfDataset(derivedFrom, false);
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
			Iterator<Changeset> iter = changes.iterator();
			while (iter.hasNext()) {
				flush(iter.next(), sink);
				iter.remove();
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
		TreeModel deprecated = change.getDeprecated();
		if (deprecated != null) {
			for (Statement st : deprecated) {
				sink.deprecate(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
			}
		}
		TreeModel approved = change.getApproved();
		if (approved != null) {
			for (Statement st : approved) {
				sink.approve(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
			}
		}
	}

}
