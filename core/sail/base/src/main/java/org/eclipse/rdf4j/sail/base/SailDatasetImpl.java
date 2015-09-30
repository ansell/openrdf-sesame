/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.common.iteration.EmptyIteration;
import org.eclipse.rdf4j.common.iteration.FilterIteration;
import org.eclipse.rdf4j.common.iteration.UnionIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.sail.SailException;

import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A view of an {@link SailSource} that is derived from a backing {@link SailDataset}.
 * 
 * @author James Leigh
 */
class SailDatasetImpl implements SailDataset {

	/**
	 * {@link SailDataset} of the backing {@link SailSource}.
	 */
	private final SailDataset derivedFrom;

	/**
	 * Changes that have not yet been {@link SailSource#flush()}ed to the backing
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
	public SailDatasetImpl(SailDataset derivedFrom, Changeset changes) {
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
					return new SimpleNamespace(e.getKey(), e.getValue());
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
		final CloseableIteration<? extends Resource, SailException> contextIDs;
		contextIDs = derivedFrom.getContextIDs();
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
	public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, IRI pred, Value obj, Resource... contexts)
		throws SailException
	{
		Set<Resource> deprecatedContexts = changes.getDeprecatedContexts();
		CloseableIteration<? extends Statement, SailException> iter;
		if (changes.isStatementCleared() || contexts == null && deprecatedContexts != null
				&& deprecatedContexts.contains(null) || contexts.length > 0 && deprecatedContexts != null
				&& deprecatedContexts.containsAll(Arrays.asList(contexts)))
		{
			iter = null;
		}
		else if (contexts.length > 0 && deprecatedContexts != null) {
			List<Resource> remaining = new ArrayList<Resource>(Arrays.asList(contexts));
			remaining.removeAll(deprecatedContexts);
			iter = derivedFrom.getStatements(subj, pred, obj, contexts);
		}
		else {
			iter = derivedFrom.getStatements(subj, pred, obj, contexts);
		}
		Model deprecated = changes.getDeprecated();
		if (deprecated != null && iter != null) {
			iter = difference(iter, deprecated.filter(subj, pred, obj, contexts));
		}
		Model approved = changes.getApproved();
		if (approved != null && iter != null) {
			return union(iter, approved.filter(subj, pred, obj, contexts));
		}
		else if (approved != null) {
			Iterator<Statement> i = approved.filter(subj, pred, obj, contexts).iterator();
			return new CloseableIteratorIteration<Statement, SailException>(i);
		}
		else if (iter != null) {
			return iter;
		}
		else {
			return new EmptyIteration<Statement, SailException>();
		}
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
