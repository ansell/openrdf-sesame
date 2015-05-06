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
import org.openrdf.sail.SailConflictException;
import org.openrdf.sail.SailException;

abstract class Changeset implements RdfSink {

	private Set<DerivedRdfDataset> refbacks;

	private Set<Changeset> prepend;

	private Set<StatementPattern> observations;

	/**
	 * Statements that have been explicitly added as part of a transaction, but
	 * has not yet been committed.
	 */
	private TreeModel approved;

	/**
	 * Explicit statements that have been deprecated and should be removed upon
	 * commit.
	 */
	private TreeModel deprecated;

	private Set<Resource> approvedContexts;

	private Set<Resource> deprecatedContexts;

	private Map<String, String> addedNamespaces;

	private Set<String> removedPrefixes;

	private boolean namespaceCleared;

	private boolean statementCleared;

	public Changeset() {
		super();
	}

	@Override
	public void close()
		throws SailException
	{
		// no-op
	}

	@Override
	public void prepare()
		throws SailException
	{
		if (prepend != null && observations != null) {
			for (StatementPattern p : observations) {
				Resource subj = (Resource)p.getSubjectVar().getValue();
				URI pred = (URI)p.getPredicateVar().getValue();
				Value obj = p.getObjectVar().getValue();
				Var ctxVar = p.getContextVar();
				Resource[] contexts;
				if (ctxVar == null) {
					contexts = new Resource[0];
				}
				else {
					contexts = new Resource[] { (Resource)ctxVar.getValue() };
				}
				for (Changeset changeset : prepend) {
					TreeModel approved = changeset.getApproved();
					TreeModel deprecated = changeset.getDeprecated();
					if (approved != null && approved.contains(subj, pred, obj, contexts) || deprecated != null
							&& deprecated.contains(subj, pred, obj, contexts))
					{
						throw new SailConflictException("Observed State has Changed");
					}
				}
			}
		}
	}

	public synchronized void addRefback(DerivedRdfDataset dataset) {
		if (refbacks == null) {
			refbacks = new HashSet<DerivedRdfDataset>();
		}
		refbacks.add(dataset);
	}

	public synchronized void removeRefback(DerivedRdfDataset dataset) {
		if (refbacks != null) {
			refbacks.remove(dataset);
		}
	}

	public synchronized boolean isRefback() {
		return refbacks != null && !refbacks.isEmpty();
	}

	public synchronized void prepend(Changeset changeset) {
		if (prepend == null) {
			prepend = new HashSet<Changeset>();
		}
		prepend.add(changeset);
	}

	@Override
	public synchronized void setNamespace(String prefix, String name) {
		if (removedPrefixes == null) {
			removedPrefixes = new HashSet<String>();
		}
		removedPrefixes.add(prefix);
		if (addedNamespaces == null) {
			addedNamespaces = new HashMap<String, String>();
		}
		addedNamespaces.put(prefix, name);
	}

	@Override
	public synchronized void removeNamespace(String prefix) {
		if (addedNamespaces != null) {
			addedNamespaces.remove(prefix);
		}
		if (removedPrefixes == null) {
			removedPrefixes = new HashSet<String>();
		}
		removedPrefixes.add(prefix);
	}

	@Override
	public synchronized void clearNamespaces() {
		if (removedPrefixes != null) {
			removedPrefixes.clear();
		}
		if (addedNamespaces != null) {
			addedNamespaces.clear();
		}
		namespaceCleared = true;
	}

	@Override
	public synchronized void observe(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailConflictException
	{
		if (observations == null) {
			observations = new HashSet<StatementPattern>();
		}
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
	public synchronized void clear(Resource... contexts) {
		if (contexts != null && contexts.length == 0) {
			if (approved != null) {
				approved.remove(null, null, null);
			}
			if (approvedContexts != null) {
				approvedContexts.clear();
			}
			statementCleared = true;
		} else {
			if (approved != null) {
				approved.remove(null, null, null, contexts);
			}
			if (approvedContexts != null) {
				approvedContexts.removeAll(Arrays.asList(contexts));
			}
			if (deprecatedContexts == null) {
				deprecatedContexts = new HashSet<Resource>();
			}
			deprecatedContexts.addAll(Arrays.asList(contexts));
		}
	}

	@Override
	public synchronized void approve(Resource subj, URI pred, Value obj, Resource ctx) {
		if (deprecated != null) {
			deprecated.remove(subj, pred, obj, ctx);
		}
		if (approved == null) {
			approved = new TreeModel();
		}
		approved.add(subj, pred, obj, ctx);
		if (ctx != null) {
			if (approvedContexts == null) {
				approvedContexts = new HashSet<Resource>();
			}
			approvedContexts.add(ctx);
		}
	}

	@Override
	public synchronized void deprecate(Resource subj, URI pred, Value obj, Resource ctx) {
		if (approved != null) {
			approved.remove(subj, pred, obj, ctx);
		}
		if (deprecated == null) {
			deprecated = new TreeModel();
		}
		deprecated.add(subj, pred, obj, ctx);
		if (approvedContexts != null && approvedContexts.contains(ctx) && !approved.contains(null, null, null, ctx))
		{
			approvedContexts.remove(ctx);
		}
	}

	public synchronized String toString() {
		StringBuilder sb = new StringBuilder();
		if (observations != null) {
			sb.append(observations).append('\n');
		}
		if (namespaceCleared) {
			sb.append("namespace cleared\n");
		}
		if (removedPrefixes != null) {
			sb.append(removedPrefixes).append('\n');
		}
		if (addedNamespaces != null) {
			sb.append(addedNamespaces).append('\n');
		}
		if (statementCleared) {
			sb.append("statements cleared\n");
		}
		if (deprecatedContexts != null && !deprecatedContexts.isEmpty()) {
			sb.append(deprecatedContexts).append('\n');
		}
		if (deprecated != null) {
			sb.append(deprecated).append('\n');
		}
		if (approved != null) {
			sb.append(approved).append('\n');
		}
		return sb.toString().trim();
	}

	public synchronized Set<StatementPattern> getObservations() {
		return observations;
	}

	public synchronized TreeModel getApproved() {
		return approved;
	}

	public synchronized TreeModel getDeprecated() {
		return deprecated;
	}

	public synchronized Set<Resource> getApprovedContexts() {
		return approvedContexts;
	}

	public synchronized Set<Resource> getDeprecatedContexts() {
		return deprecatedContexts;
	}

	public synchronized boolean isStatementCleared() {
		return statementCleared;
	}

	public synchronized Map<String, String> getAddedNamespaces() {
		return addedNamespaces;
	}

	public synchronized Set<String> getRemovedPrefixes() {
		return removedPrefixes;
	}

	public synchronized boolean isNamespaceCleared() {
		return namespaceCleared;
	}
}