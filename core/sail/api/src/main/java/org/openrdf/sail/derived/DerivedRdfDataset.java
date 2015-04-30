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
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.FilterIteration;
import info.aduna.iteration.UnionIteration;

import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.sail.SailException;

class DerivedRdfDataset implements RdfDataset {

	private final RdfDataset derivedFrom;

	private final Changeset changes;

	/**
	 * Create a derivative dataset that applies the given changeset. The life
	 * cycle of this and the given {@link RdfDataset} are bound.
	 * 
	 * @param derivedFrom
	 *        will be released when this object is released
	 * @param changes
	 *        changeset to be observed with the given dataset
	 */
	public DerivedRdfDataset(RdfDataset derivedFrom, Changeset changes) {
		this.derivedFrom = derivedFrom;
		this.changes = changes;
		changes.addRefback(this);
	}

	@Override
	public boolean isActive() {
		return derivedFrom.isActive();
	}

	@Override
	public void release() {
		derivedFrom.release();
		changes.removeRefback(this);
	}

	@Override
	public String getNamespace(String prefix)
		throws SailException
	{
		if (changes.getAddedNamespaces().containsKey(prefix))
			return changes.getAddedNamespaces().get(prefix);
		if (changes.getRemovedPrefixes().contains(prefix) || changes.isNamespaceCleared())
			return null;
		return derivedFrom.getNamespace(prefix);
	}

	@Override
	public CloseableIteration<? extends Namespace, SailException> getNamespaces()
		throws SailException
	{
		final CloseableIteration<? extends Namespace, SailException> namespaces;
		if (changes.isNamespaceCleared()) {
			namespaces = new EmptyIteration<Namespace, SailException>();
		}
		else {
			namespaces = derivedFrom.getNamespaces();
		}
		Iterator<Map.Entry<String, String>> added = null;
		Set<String> removed = null;
		synchronized (this) {
			if (!changes.getAddedNamespaces().isEmpty()) {
				added = changes.getAddedNamespaces().entrySet().iterator();
			}
			if (!changes.getRemovedPrefixes().isEmpty()) {
				removed = changes.getRemovedPrefixes();
			}
		}
		if (added == null && removed == null)
			return namespaces;
		final Iterator<Map.Entry<String, String>> addedIter = added;
		final Set<String> removedSet = removed;
		return new CloseableIteration<Namespace, SailException>() {

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
	public CloseableIteration<? extends Resource, SailException> getContextIDs()
		throws SailException
	{
		final CloseableIteration<? extends Resource, SailException> contextIDs = derivedFrom.getContextIDs();
		Iterator<Resource> added = null;
		Set<Resource> removed = null;
		synchronized (this) {
			if (!changes.getAddedContexts().isEmpty()) {
				added = changes.getAddedContexts().iterator();
			}
			if (!changes.getRemovedContexts().isEmpty()) {
				removed = changes.getRemovedContexts();
			}
		}
		if (added == null && removed == null)
			return contextIDs;
		final Iterator<Resource> addedIter = added;
		final Set<Resource> removedSet = removed;
		return new CloseableIteration<Resource, SailException>() {

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
	public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, URI pred,
			Value obj, Resource... contexts)
		throws SailException
	{
		return union(
				union(difference(
						difference(derivedFrom.getStatements(subj, pred, obj, contexts),
								changes.getDeprecated().filter(subj, pred, obj, contexts)),
						changes.getInvalid().filter(subj, pred, obj, contexts)),
						changes.getExplicit().filter(subj, pred, obj, contexts)),
				changes.getInferred().filter(subj, pred, obj, contexts));
	}

	@Override
	public CloseableIteration<? extends Statement, SailException> getExplicit(Resource subj, URI pred,
			Value obj, Resource... contexts)
		throws SailException
	{
		return union(
				difference(derivedFrom.getExplicit(subj, pred, obj, contexts),
						changes.getDeprecated().filter(subj, pred, obj, contexts)),
				changes.getExplicit().filter(subj, pred, obj, contexts));
	}

	@Override
	public CloseableIteration<? extends Statement, SailException> getInferred(Resource subj, URI pred,
			Value obj, Resource... contexts)
		throws SailException
	{
		return union(
				difference(derivedFrom.getInferred(subj, pred, obj, contexts),
						changes.getInvalid().filter(subj, pred, obj, contexts)),
				changes.getInferred().filter(subj, pred, obj, contexts));
	}

	private CloseableIteration<? extends Statement, SailException> difference(
			CloseableIteration<? extends Statement, SailException> result, final Model excluded)
	{
		if (excluded.isEmpty()) {
			return result;
		}
		return new FilterIteration<Statement, SailException>(result) {

			protected boolean accept(Statement stmt) {
				return !excluded.contains(stmt);
			}
		};
	}

	private CloseableIteration<? extends Statement, SailException> union(
			CloseableIteration<? extends Statement, SailException> result, Model included)
	{
		if (included.isEmpty()) {
			return result;
		}
		final Iterator<Statement> iter = included.iterator();
		CloseableIteration<Statement, SailException> incl;
		incl = new CloseableIteratorIteration<Statement, SailException>(iter);
		return new UnionIteration<Statement, SailException>(incl, result);
	}

}