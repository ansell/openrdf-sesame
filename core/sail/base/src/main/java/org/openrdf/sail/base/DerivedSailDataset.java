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
package org.openrdf.sail.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;

import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.sail.SailException;

/**
 * A view of an {@link SailBranch} that is based on a backing {@link SailSource}.
 * 
 * @author James Leigh
 */
class DerivedSailDataset implements SailDataset {

	/**
	 * {@link SailDataset} of the backing {@link SailSource}.
	 */
	private final SailDataset derivedFrom;

	/**
	 * Changes that have not yet been {@link SailBranch#flush()}ed to the backing
	 * {@link SailDataset}.
	 */
	private final Changeset changes;

	/**
	 * Create a derivative dataset that applies the given changeset. The life
	 * cycle of this and the given {@link SailDataset} are bound.
	 * 
	 * @param derivedFrom
	 *        will be released when this object is released
	 * @param changes
	 *        changeset to be observed with the given dataset
	 */
	public DerivedSailDataset(SailDataset derivedFrom, Changeset changes) {
		this.derivedFrom = derivedFrom;
		this.changes = changes;
		changes.addRefback(this);
	}

	public String toString() {
		return changes + "\n" + derivedFrom;
	}

	@Override
	public void close()
		throws SailException
	{
		changes.removeRefback(this);
		derivedFrom.close();
	}

	@Override
	public String getNamespace(String prefix)
		throws SailException
	{
		Map<String, String> addedNamespaces = changes.getAddedNamespaces();
		if (addedNamespaces != null && addedNamespaces.containsKey(prefix))
			return addedNamespaces.get(prefix);
		Set<String> removedPrefixes = changes.getRemovedPrefixes();
		if (removedPrefixes != null && removedPrefixes.contains(prefix) || changes.isNamespaceCleared())
			return null;
		return derivedFrom.getNamespace(prefix);
	}

	@Override
	public SailIteration<? extends Namespace> getNamespaces()
		throws SailException
	{
		final SailIteration<? extends Namespace> namespaces;
		if (changes.isNamespaceCleared()) {
			namespaces = EmptySailIteration.emptyIteration();
		}
		else {
			namespaces = derivedFrom.getNamespaces();
		}
		Iterator<Map.Entry<String, String>> added = null;
		Set<String> removed = null;
		synchronized (this) {
			Map<String, String> addedNamespaces = changes.getAddedNamespaces();
			if (addedNamespaces != null) {
				added = addedNamespaces.entrySet().iterator();
			}
			removed = changes.getRemovedPrefixes();
		}
		if (added == null && removed == null)
			return namespaces;
		final Iterator<Map.Entry<String, String>> addedIter = added;
		final Set<String> removedSet = removed;
		return new SailIteration<Namespace>() {

			Namespace next;

			public boolean hasNext()
				throws SailException
			{
				if (addedIter != null && addedIter.hasNext())
					return true;
				while (next == null && namespaces.hasNext()) {
					next = namespaces.next();
					if (removedSet != null && removedSet.contains(next.getPrefix())) {
						next = null;
					}
				}
				return next != null;
			}

			public Namespace next()
				throws SailException
			{
				if (addedIter != null && addedIter.hasNext()) {
					Entry<String, String> e = addedIter.next();
					return new NamespaceImpl(e.getKey(), e.getValue());
				}
				try {
					if (hasNext())
						return next;
					throw new NoSuchElementException();
				}
				finally {
					next = null;
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void close()
				throws SailException
			{
				namespaces.close();
			}
		};
	}

	@Override
	public SailIteration<? extends Resource> getContextIDs()
		throws SailException
	{
		final SailIteration<? extends Resource> contextIDs = derivedFrom.getContextIDs();
		Iterator<Resource> added = null;
		Set<Resource> removed = null;
		synchronized (this) {
			Set<Resource> approvedContexts = changes.getApprovedContexts();
			if (approvedContexts != null) {
				added = approvedContexts.iterator();
			}
			Set<Resource> deprecatedContexts = changes.getDeprecatedContexts();
			if (deprecatedContexts != null) {
				removed = deprecatedContexts;
			}
		}
		if (added == null && removed == null)
			return contextIDs;
		final Iterator<Resource> addedIter = added;
		final Set<Resource> removedSet = removed;
		return new SailIteration<Resource>() {

			Resource next;

			public void close()
				throws SailException
			{
				contextIDs.close();
			}

			public boolean hasNext()
				throws SailException
			{
				if (addedIter != null && addedIter.hasNext())
					return true;
				while (next == null && contextIDs.hasNext()) {
					next = contextIDs.next();
					if (removedSet != null && removedSet.contains(next)) {
						next = null;
					}
				}
				return next != null;
			}

			public Resource next()
				throws SailException
			{
				if (addedIter != null && addedIter.hasNext())
					return addedIter.next();
				try {
					if (hasNext())
						return next;
					throw new NoSuchElementException();
				}
				finally {
					next = null;
				}
			}

			public void remove()
				throws SailException
			{
				throw new UnsupportedOperationException();
			}

		};
	}

	@Override
	public SailIteration<? extends Statement> get(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		Set<Resource> deprecatedContexts = changes.getDeprecatedContexts();
		SailIteration<? extends Statement> iter;
		if (changes.isStatementCleared() || contexts == null && deprecatedContexts != null
				&& deprecatedContexts.contains(null) || contexts.length > 0 && deprecatedContexts != null
				&& deprecatedContexts.containsAll(Arrays.asList(contexts)))
		{
			iter = null;
		}
		else if (contexts.length > 0 && deprecatedContexts != null) {
			List<Resource> remaining = new ArrayList<Resource>(Arrays.asList(contexts));
			remaining.removeAll(deprecatedContexts);
			iter = derivedFrom.get(subj, pred, obj, contexts);
		}
		else {
			iter = derivedFrom.get(subj, pred, obj, contexts);
		}
		Model deprecated = changes.getDeprecated();
		if (deprecated != null && iter != null) {
			iter = difference(iter, deprecated.filter(subj, pred, obj, contexts));
		}
		Model approved = changes.getApproved();
		if (approved != null && iter != null) {
			return union(iter, approved.filter(subj, pred, obj, contexts));
		} else if (approved != null) {
			return ClosingSailIteration.close(approved.filter(subj, pred, obj, contexts).iterator());
		} else if (iter != null) {
			return iter;
		} else {
			return EmptySailIteration.emptyIteration();
		}
	}

	private SailIteration<? extends Statement> difference(SailIteration<? extends Statement> result,
			final Model excluded)
	{
		if (excluded.isEmpty()) {
			return result;
		}
		return new FilterSailIteration<Statement>(result) {

			protected boolean accept(Statement stmt) {
				return !excluded.contains(stmt);
			}
		};
	}

	private SailIteration<? extends Statement> union(SailIteration<? extends Statement> result, Model included) {
		if (included.isEmpty()) {
			return result;
		}
		final Iterator<Statement> iter = included.iterator();
		CloseableIteration<Statement, SailException> incl;
		incl = new CloseableIteratorIteration<Statement, SailException>(iter);
		return new UnionSailIteration<Statement>(incl, result);
	}

}