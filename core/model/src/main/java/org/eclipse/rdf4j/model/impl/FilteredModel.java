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
package org.eclipse.rdf4j.model.impl;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.eclipse.rdf4j.OpenRDFUtil;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

/**
 * Applies a basic graph pattern filter to what triples can be see.
 * 
 * @since 2.7.0
 */
public abstract class FilteredModel extends AbstractModel {

	private final Model model;

	private static final long serialVersionUID = -2353344619836326934L;

	protected Resource subj;

	protected IRI pred;

	protected Value obj;

	protected Resource[] contexts;

	public FilteredModel(AbstractModel model, Resource subj, IRI pred, Value obj, Resource... contexts) {
		OpenRDFUtil.verifyContextNotNull(contexts);

		this.model = model;
		this.subj = subj;
		this.pred = pred;
		this.obj = obj;
		this.contexts = contexts;
	}

	@Override
	public Optional<Namespace> getNamespace(String prefix) {
		return model.getNamespace(prefix);
	}

	@Override
	public Set<Namespace> getNamespaces() {
		return model.getNamespaces();
	}

	@Override
	public Namespace setNamespace(String prefix, String name) {
		return model.setNamespace(prefix, name);
	}

	@Override
	public void setNamespace(Namespace namespace) {
		this.model.setNamespace(namespace);
	}

	@Override
	public Optional<Namespace> removeNamespace(String prefix) {
		return model.removeNamespace(prefix);
	}

	@Override
	public int size() {
		Iterator<Statement> iter = iterator();
		try {
			int size = 0;
			while (iter.hasNext()) {
				size++;
				iter.next();
			}
			return size;
		}
		finally {
			closeIterator(iter);
		}
	}

	@Override
	public boolean add(Resource s, IRI p, Value o, Resource... c) {
		if (s == null) {
			s = subj;
		}
		if (p == null) {
			p = pred;
		}
		if (o == null) {
			o = obj;
		}
		if (c != null && c.length == 0) {
			c = contexts;
		}
		if (!accept(s, p, o, c)) {
			throw new IllegalArgumentException("Statement is filtered out of view");
		}
		return model.add(s, p, o, c);
	}

	@Override
	public boolean remove(Resource s, IRI p, Value o, Resource... c) {
		if (s == null) {
			s = subj;
		}
		if (p == null) {
			p = pred;
		}
		if (o == null) {
			o = obj;
		}
		if (c != null && c.length == 0) {
			c = contexts;
		}
		if (!accept(s, p, o, c)) {
			return false;
		}
		return model.remove(s, p, o, c);
	}

	@Override
	public boolean contains(Resource s, IRI p, Value o, Resource... c) {
		if (s == null) {
			s = subj;
		}
		if (p == null) {
			p = pred;
		}
		if (o == null) {
			o = obj;
		}
		if (c != null && c.length == 0) {
			c = contexts;
		}
		if (!accept(s, p, o, c)) {
			return false;
		}
		return model.contains(s, p, o, c);
	}

	@Override
	public Model filter(Resource s, IRI p, Value o, Resource... c) {
		if (s == null) {
			s = subj;
		}
		if (p == null) {
			p = pred;
		}
		if (o == null) {
			o = obj;
		}
		if (c != null && c.length == 0) {
			c = contexts;
		}
		if (!accept(s, p, o, c)) {
			return new EmptyModel(model);
		}
		return model.filter(s, p, o, c);
	}

	@Override
	public final void removeTermIteration(Iterator<Statement> iter, Resource s, IRI p, Value o, Resource... c)
	{
		if (s == null) {
			s = subj;
		}
		if (p == null) {
			p = pred;
		}
		if (o == null) {
			o = obj;
		}
		if (c != null && c.length == 0) {
			c = contexts;
		}
		if (!accept(s, p, o, c)) {
			throw new IllegalStateException();
		}
		removeFilteredTermIteration(iter, s, p, o, c);
	}

	/**
	 * Called by aggregate sets when a term has been removed from a term
	 * iterator. At least one of the last four terms will be non-empty.
	 * 
	 * @param iter
	 *        The iterator used to navigate the live set (never null)
	 * @param subj
	 *        the subject term to be removed or null
	 * @param pred
	 *        the predicate term to be removed or null
	 * @param obj
	 *        the object term to be removed or null
	 * @param contexts
	 *        an array of one context term to be removed or an empty array
	 */
	protected abstract void removeFilteredTermIteration(Iterator<Statement> iter, Resource subj, IRI pred,
			Value obj, Resource... contexts);

	private boolean accept(Resource s, IRI p, Value o, Resource... c) {
		if (subj != null && !subj.equals(s)) {
			return false;
		}
		if (pred != null && !pred.equals(p)) {
			return false;
		}
		if (obj != null && !obj.equals(o)) {
			return false;
		}
		if (!matches(c, contexts)) {
			return false;
		}
		return (s == null || s instanceof Resource) && (p == null || p instanceof IRI);
	}

	private boolean matches(Resource[] stContext, Resource... contexts) {
		OpenRDFUtil.verifyContextNotNull(stContext);
		if (stContext != null && stContext.length > 0) {
			for (Resource c : stContext) {
				if (!matches(c, contexts)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean matches(Resource stContext, Resource... contexts) {
		if (contexts != null && contexts.length == 0) {
			// Any context matches
			return stContext == null || stContext instanceof Resource;
		}
		else {
			OpenRDFUtil.verifyContextNotNull(contexts);
			// Accept if one of the contexts from the pattern matches
			for (Resource context : contexts) {
				if (context == null && stContext == null) {
					return true;
				}
				if (context != null && context.equals(stContext)) {
					return true;
				}
			}

			return false;
		}
	}
}
