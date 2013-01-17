/*
 * Copyright (c) 2012, 3 Round Stones Inc. Some rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution. 
 * - Neither the name of the openrdf.org nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package org.openrdf.model.impl;

import java.util.Iterator;
import java.util.Map;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * Applies a basic graph pattern filter to what triples can be see.
 */
public abstract class FilteredModel extends AbstractModel {
	private final Model model;

	private static final long serialVersionUID = -2353344619836326934L;

	private Value subj;

	private Value pred;

	private Value obj;

	private Value[] contexts;

	public FilteredModel(AbstractModel model, Value subj, Value pred, Value obj,
			Value... contexts) {
		this.model = model;
		this.subj = subj;
		this.pred = pred;
		this.obj = obj;
		this.contexts = notNull(contexts);
	}

	public String getNamespace(String prefix) {
		return model.getNamespace(prefix);
	}

	public Map<String, String> getNamespaces() {
		return model.getNamespaces();
	}

	public String setNamespace(String prefix, String name) {
		return model.setNamespace(prefix, name);
	}

	public String removeNamespace(String prefix) {
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
		} finally {
			closeIterator(iter);
		}
	}

	public boolean add(Resource s, URI p, Value o, Resource... c) {
		if (s == null) {
			s = (Resource) subj;
		}
		if (p == null) {
			p = (URI) pred;
		}
		if (o == null) {
			o = obj;
		}
		if (c != null && c.length == 0) {
			c = cast(contexts);
		}
		if (!accept(s, p, o, c)) {
			throw new IllegalArgumentException(
					"Statement is filtered out of view");
		}
		return model.add(s, p, o, c);
	}

	public boolean remove(Value s, Value p, Value o, Value... c) {
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

	public boolean contains(Value s, Value p, Value o, Value... c) {
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

	public Model filter(Value s, Value p, Value o, Value... c) {
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
	public final void removeTermIteration(Iterator<Statement> iter, Resource s, URI p,
			Value o, Resource... c) {
		if (s == null) {
			s = (Resource) subj;
		}
		if (p == null) {
			p = (URI) pred;
		}
		if (o == null) {
			o = obj;
		}
		if (c != null && c.length == 0) {
			c = cast(contexts);
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
	 *            The iterator used to navigate the live set (never null)
	 * @param subj
	 *            the subject term to be removed or null
	 * @param pred
	 *            the predicate term to be removed or null
	 * @param obj
	 *            the object term to be removed or null
	 * @param contexts
	 *            an array of one context term to be removed or an empty array
	 */
	protected abstract void removeFilteredTermIteration(Iterator<Statement> iter,
			Resource subj, URI pred, Value obj, Resource... contexts);

	private boolean accept(Value s, Value p, Value o, Value... c) {
		if (subj != null && !subj.equals(s)) {
			return false;
		}
		if (pred != null && !pred.equals(p)) {
			return false;
		}
		if (obj != null && !obj.equals(o)) {
			return false;
		}
		if (!matches(notNull(c), contexts)) {
			return false;
		}
		return (s == null || s instanceof Resource) && (p == null || p instanceof URI);
	}

	private boolean matches(Value[] stContext, Value... contexts) {
		if (stContext != null && stContext.length > 0) {
			for (Value c : stContext) {
				if (!matches(c, contexts)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean matches(Value stContext, Value... contexts) {
		if (contexts != null && contexts.length == 0) {
			// Any context matches
			return stContext == null || stContext instanceof Resource;
		} else {
			// Accept if one of the contexts from the pattern matches
			for (Value context : notNull(contexts)) {
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

	private Resource[] cast(Value[] contexts) {
		if (contexts instanceof Resource[])
			return (Resource[]) contexts;
		if (contexts == null)
			return new Resource[] { null };
		if (contexts.length == 0)
			return new Resource[0];
		Resource[] result = new Resource[contexts.length];
		System.arraycopy(contexts, 0, result, 0, contexts.length);
		return result;
	}

	private Value[] notNull(Value[] contexts) {
		if (contexts == null)
			return new Resource[] { null };
		if (contexts.length == 0)
			return new Resource[0];
		return contexts;
	}
}
