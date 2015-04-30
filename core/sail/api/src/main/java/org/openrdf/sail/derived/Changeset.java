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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;

class Changeset implements RdfSink {

	private final Set<DerivedRdfDataset> refbacks = new HashSet<DerivedRdfDataset>();

	private final Set<StatementPattern> observations = new HashSet<StatementPattern>();

	/**
	 * Statements that have been explicitly added as part of a transaction, but
	 * has not yet been committed.
	 */
	private final TreeModel explicit = new TreeModel();

	/**
	 * Explicit statements that have been deprecated and should be removed upon
	 * commit.
	 */
	private final TreeModel deprecated = new TreeModel();

	/**
	 * Statements that can now be inferred from the other statements. This or
	 * another existing explicit statement has been added or removed.
	 */
	private final TreeModel inferred = new TreeModel();

	/**
	 * Inferred statements that do not hold and should be removed upon commit.
	 */
	private final TreeModel invalid = new TreeModel();

	private final Set<Resource> addedContexts = new HashSet<Resource>();

	private final Set<Resource> removedContexts = new HashSet<Resource>();

	private final Map<String, String> addedNamespaces = new HashMap<String, String>();

	private final Set<String> removedPrefixes = new HashSet<String>();

	private boolean namespaceCleared;

	private boolean statementCleared;

	private boolean released;

	public Changeset() {
		super();
	}

	@Override
	public boolean isActive() {
		return !released;
	}

	@Override
	public void release() {
		released = true;
	}

	public void addRefback(DerivedRdfDataset dataset) {
		refbacks.add(dataset);
	}

	public void removeRefback(DerivedRdfDataset dataset) {
		refbacks.remove(dataset);
	}

	public boolean isRefback() {
		return !refbacks.isEmpty();
	}

	@Override
	public synchronized void setNamespace(String prefix, String name) {
		assert isActive();
		removedPrefixes.add(prefix);
		addedNamespaces.put(prefix, name);
	}

	@Override
	public void removeNamespace(String prefix) {
		assert isActive();
		removedPrefixes.add(prefix);
		addedNamespaces.remove(prefix);
	}

	@Override
	public void clearNamespaces() {
		assert isActive();
		removedPrefixes.clear();
		addedNamespaces.clear();
		namespaceCleared = true;
	}

	@Override
	public void observe(Resource subj, URI pred, Value obj, Resource... contexts) {
		if (contexts == null) {
			observations.add(new StatementPattern(new Var("s", subj), new Var("p", pred), new Var("o", obj),
					new Var("g", null)));
		}
		else if (contexts.length == 0) {
			observations.add(new StatementPattern(new Var("s", subj), new Var("p", pred), new Var("o", obj)));
		}
		else {
			for (Resource ctx : contexts) {
				observations.add(new StatementPattern(new Var("s", subj), new Var("p", pred), new Var("o", obj),
						new Var("g", ctx)));
			}
		}
	}

	@Override
	public void clear(Resource... contexts) {
		if (contexts != null && contexts.length == 0) {
			explicit.remove(null, null, null);
			inferred.remove(null, null, null);
			addedContexts.clear();
			statementCleared = true;
		} else {
			explicit.remove(null, null, null, contexts);
			inferred.remove(null, null, null, contexts);
			addedContexts.removeAll(Arrays.asList(contexts));
			removedContexts.addAll(Arrays.asList(contexts));
		}
	}

	@Override
	public void addExplicit(Resource subj, URI pred, Value obj, Resource ctx) {
		assert isActive();
		deprecated.remove(subj, pred, obj, ctx);
		explicit.add(subj, pred, obj, ctx);
		if (ctx != null) {
			addedContexts.add(ctx);
		}
	}

	@Override
	public void removeExplicit(Resource subj, URI pred, Value obj, Resource ctx) {
		assert isActive();
		explicit.remove(subj, pred, obj, ctx);
		deprecated.add(subj, pred, obj, ctx);
		if (addedContexts.contains(ctx) && !explicit.contains(null, null, null, ctx)
				&& !inferred.contains(null, null, null, ctx))
		{
			addedContexts.remove(ctx);
		}
	}

	@Override
	public void addInferred(Resource subj, URI pred, Value obj, Resource ctx) {
		assert isActive();
		invalid.remove(subj, pred, obj, ctx);
		inferred.add(subj, pred, obj, ctx);
		if (ctx != null) {
			addedContexts.add(ctx);
		}
	}

	@Override
	public void removeInferred(Resource subj, URI pred, Value obj, Resource ctx) {
		assert isActive();
		inferred.remove(subj, pred, obj, ctx);
		invalid.add(subj, pred, obj, ctx);
		if (addedContexts.contains(ctx) && !explicit.contains(null, null, null, ctx)
				&& !inferred.contains(null, null, null, ctx))
		{
			addedContexts.remove(ctx);
		}
	}

	public Set<StatementPattern> getObservations() {
		return observations;
	}

	public TreeModel getExplicit() {
		return explicit;
	}

	public TreeModel getDeprecated() {
		return deprecated;
	}

	public TreeModel getInferred() {
		return inferred;
	}

	public TreeModel getInvalid() {
		return invalid;
	}

	public Set<Resource> getAddedContexts() {
		return addedContexts;
	}

	public Set<Resource> getRemovedContexts() {
		return removedContexts;
	}

	public boolean isStatementCleared() {
		return statementCleared;
	}

	public Map<String, String> getAddedNamespaces() {
		return addedNamespaces;
	}

	public Set<String> getRemovedPrefixes() {
		return removedPrefixes;
	}

	public boolean isNamespaceCleared() {
		return namespaceCleared;
	}
}