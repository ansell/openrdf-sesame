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

import org.openrdf.IsolationLevel;
import org.openrdf.IsolationLevels;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.sail.SailException;

/**
 * @author James Leigh
 */
public class DerivedRDfSource implements RdfSource {

	private final LinkedList<Changeset> changes = new LinkedList<Changeset>();

	private final RdfSource backingSource;

	/**
	 * Non-null when in {@link IsolationLevels#SNAPSHOT} (or higher) mode.
	 */
	private RdfDataset snapshot;

	/**
	 * Non-null when in {@link IsolationLevels#SERIALIZABLE} (or higher) mode.
	 */
	private RdfSink serializable;

	private boolean released;

	public DerivedRDfSource(RdfSource backingSource) {
		this.backingSource = backingSource;
	}

	@Override
	public boolean isActive() {
		return !released;
	}

	@Override
	public void release() {
		released = true;
		if (snapshot != null) {
			snapshot.release();
			snapshot = null;
		}
		if (serializable != null) {
			serializable.release();
			serializable = null;
		}
	}

	@Override
	public RdfSink sink(IsolationLevel level)
		throws SailException
	{
		return new Changeset() {

			@Override
			public void release() {
				super.release();
				if (isChanged(this)) {
					merge(this);
				}
			}
		};
	}

	@Override
	public RdfDataset snapshot(IsolationLevel level)
		throws SailException
	{
		return new DelegatingRdfDataset(derivedFromSerializable(level), true) {

			@Override
			public void release() {
				super.release();
				compressChanges();
			}
		};
	}

	@Override
	public RdfSource fork()
		throws SailException
	{
		return new DerivedRDfSource(this);
	}

	@Override
	public void prepare()
		throws SailException
	{
		if (serializable == null) {
			serializable = backingSource.sink(IsolationLevels.READ_UNCOMMITTED);
		}
		prepare(serializable);
		backingSource.prepare();
	}

	@Override
	public void flush()
		throws SailException
	{
		if (serializable == null) {
			prepare();
		}
		flush(serializable);
		backingSource.flush();
	}

	void merge(Changeset change) {
		synchronized (changes) {
			changes.add(change);
			compressChanges();
		}
	}

	void compressChanges() {
		synchronized (changes) {
			while (changes.size() > 1 && !changes.get(changes.size() - 2).isRefback()) {
				try {
					flush(changes.removeLast(), changes.getLast());
				}
				catch (SailException e) {
					// Changeset does not throw SailException
					throw new AssertionError(e);
				}
			}
		}
	}

	private boolean isChanged(Changeset change) {
		return !change.getExplicit().isEmpty() || !change.getDeprecated().isEmpty()
				|| !change.getInferred().isEmpty() || !change.getInvalid().isEmpty()
				|| !change.getAddedContexts().isEmpty() || !change.getRemovedContexts().isEmpty()
				|| !change.getAddedNamespaces().isEmpty() || !change.getRemovedPrefixes().isEmpty()
				|| change.isStatementCleared() || change.isNamespaceCleared();
	}

	private RdfDataset derivedFromSerializable(IsolationLevel level)
		throws SailException
	{
		if (serializable == null && level.isCompatibleWith(IsolationLevels.SERIALIZABLE)) {
			serializable = backingSource.sink(level);
		}
		synchronized (changes) {
			RdfDataset derivedFrom = derivedFromSnapshot(level);
			if (serializable == null) {
				return derivedFrom;
			}
			else if (changes.isEmpty()) {
				// change an empty changeset to just track observations
				Changeset change = new Changeset();
				merge(change);
				return new ObservingRdfDataset(derivedFrom, change);
			}
			else {
				return new ObservingRdfDataset(derivedFrom, changes.getLast());
			}
		}
	}

	private RdfDataset derivedFromSnapshot(IsolationLevel level)
		throws SailException
	{
		synchronized (changes) {
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
	}

	private void prepare(RdfSink sink)
		throws SailException
	{
		synchronized (changes) {
			Iterator<Changeset> iter = changes.iterator();
			while (iter.hasNext()) {
				prepare(iter.next(), sink);
			}
		}
	}

	private void prepare(Changeset change, RdfSink sink)
		throws SailException
	{
		for (StatementPattern p : change.getObservations()) {
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

	private void flush(RdfSink sink)
		throws SailException
	{
		synchronized (changes) {
			Iterator<Changeset> iter = changes.iterator();
			while (iter.hasNext()) {
				flush(iter.next(), sink);
				iter.remove();
			}
		}
	}

	private void flush(Changeset change, RdfSink sink)
		throws SailException
	{
		prepare(change, sink);
		if (change.isNamespaceCleared()) {
			sink.clearNamespaces();
		}
		for (String prefix : change.getRemovedPrefixes()) {
			sink.removeNamespace(prefix);
		}
		for (Map.Entry<String, String> e : change.getAddedNamespaces().entrySet()) {
			sink.setNamespace(e.getKey(), e.getValue());
		}
		if (change.isStatementCleared()) {
			sink.clear();
		}
		if (!change.getRemovedContexts().isEmpty()) {
			sink.clear(change.getRemovedContexts().toArray(new Resource[change.getRemovedContexts().size()]));
		}
		for (Statement st : change.getDeprecated()) {
			sink.removeExplicit(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
		}
		for (Statement st : change.getExplicit()) {
			sink.addExplicit(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
		}
		for (Statement st : change.getInvalid()) {
			sink.removeInferred(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
		}
		for (Statement st : change.getInferred()) {
			sink.addInferred(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
		}
	}

}
