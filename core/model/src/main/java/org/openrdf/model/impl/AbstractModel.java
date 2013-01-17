/*
 * Copyright (c) 2012-2013	, 3 Round Stones Inc. Some rights reserved.
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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.util.ModelException;
import org.openrdf.model.util.ModelUtil;

/**
 * Provides basic operations that are common to all Models.
 */
public abstract class AbstractModel extends AbstractSet<Statement> implements
		Model {
	private static final long serialVersionUID = 4254119331281455614L;

	public Model unmodifiable() {
		return new UnmodifiableModel(this);
	}

	@Override
	public boolean add(Statement st) {
		return add(st.getSubject(), st.getPredicate(), st.getObject(),
				st.getContext());
	}

	@Override
	public boolean isEmpty() {
		return !contains(null, null, null);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		Iterator<?> e = c.iterator();
		try {
			while (e.hasNext())
			    if (!contains(e.next()))
			        return false;
			return true;
		} finally {
			closeIterator(c, e);
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean modified = false;
		if (size() > c.size()) {
			Iterator<?> i = c.iterator();
			try {
			    while (i.hasNext())
			        modified |= remove(i.next());
			} finally {
				closeIterator(c, i);
			}
		} else {
			Iterator<?> i = iterator();
			try {
			    while (i.hasNext()) {
			        if (c.contains(i.next())) {
			            i.remove();
			            modified = true;
			        }
			    }
			} finally {
				closeIterator(i);
			}
		}
		return modified;
	}

	@Override
	public Object[] toArray() {
		// Estimate size of array; be prepared to see more or fewer elements
		Iterator<Statement> it = iterator();
		try {
			List<Object> r = new ArrayList<Object>(size());
			while (it.hasNext()) {
				r.add(it.next());
			}
			return r.toArray();
		} finally {
			closeIterator(it);
		}
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// Estimate size of array; be prepared to see more or fewer elements
		Iterator<Statement> it = iterator();
		try {
			List<Object> r = new ArrayList<Object>(size());
			while (it.hasNext()) {
				r.add(it.next());
			}
			return r.toArray(a);
		} finally {
			closeIterator(it);
		}
	}

	@Override
	public boolean addAll(Collection<? extends Statement> c) {
		Iterator<? extends Statement> e = c.iterator();
		try {
			boolean modified = false;
			while (e.hasNext()) {
			    if (add(e.next()))
			        modified = true;
			}
			return modified;
		} finally {
			closeIterator(c, e);
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		Iterator<Statement> e = iterator();
		try {
			boolean modified = false;
			while (e.hasNext()) {
			    if (!c.contains(e.next())) {
			        e.remove();
			        modified = true;
			    }
			}
			return modified;
		} finally {
			closeIterator(e);
		}
	}

	@Override
	public void clear() {
		remove(null, null, null);
	}

	public boolean clear(Value... contexts) {
		return remove(null, null, null, contexts);
	}

	@Override
	public boolean remove(Object o) {
		if (o instanceof Statement) {
			Statement st = (Statement) o;
			return remove(st.getSubject(), st.getPredicate(), st.getObject(),
					st.getContext());
		}
		return false;
	}

	@Override
	public boolean contains(Object o) {
		if (o instanceof Statement) {
			Statement st = (Statement) o;
			return contains(st.getSubject(), st.getPredicate(), st.getObject(),
					st.getContext());
		}
		return false;
	}

	public Value objectValue() throws ModelException {
		Iterator<Value> iter = objects().iterator();
		try {
			if (iter.hasNext()) {
				Value obj = iter.next();
				if (iter.hasNext()) {
					throw new ModelException(obj, iter.next());
				}
				return obj;
			}
			return null;
		} finally {
			closeIterator(iter);
		}
	}

	public Literal objectLiteral() throws ModelException {
		Value obj = objectValue();
		if (obj == null) {
			return null;
		}
		if (obj instanceof Literal) {
			return (Literal) obj;
		}
		throw new ModelException(obj);
	}

	public Resource objectResource() throws ModelException {
		Value obj = objectValue();
		if (obj == null) {
			return null;
		}
		if (obj instanceof Resource) {
			return (Resource) obj;
		}
		throw new ModelException(obj);
	}

	public URI objectURI() throws ModelException {
		Value obj = objectValue();
		if (obj == null) {
			return null;
		}
		if (obj instanceof URI) {
			return (URI) obj;
		}
		throw new ModelException(obj);
	}

	public String objectString() throws ModelException {
		Value obj = objectValue();
		if (obj == null) {
			return null;
		}
		return obj.stringValue();
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof Model) {
			Model model = (Model) o;
			return ModelUtil.equals(this, model);
		}
		return false;
	}

	public Set<Resource> subjects() {
		return new ValueSet<Resource>() {

			@Override
			public boolean contains(Object o) {
				if (o instanceof Resource) {
					return AbstractModel.this
							.contains((Resource) o, null, null);
				}
				return false;
			}

			@Override
			public boolean remove(Object o) {
				if (o instanceof Resource) {
					return AbstractModel.this.remove((Resource) o, null, null);
				}
				return false;
			}

			@Override
			public boolean add(Resource subj) {
				return AbstractModel.this.add(subj, null, null);
			}

			@Override
			protected Resource term(Statement st) {
				return st.getSubject();
			}

			@Override
			protected void removeIteration(Iterator<Statement> iter,
					Resource subj) {
				AbstractModel.this.removeTermIteration(iter, subj, null, null);
			}
		};
	}

	public Set<URI> predicates() {
		return new ValueSet<URI>() {

			@Override
			public boolean contains(Object o) {
				if (o instanceof URI) {
					return AbstractModel.this.contains(null, (URI) o, null);
				}
				return false;
			}

			@Override
			public boolean remove(Object o) {
				if (o instanceof URI) {
					return AbstractModel.this.remove(null, (URI) o, null);
				}
				return false;
			}

			@Override
			public boolean add(URI pred) {
				return AbstractModel.this.add(null, pred, null);
			}

			@Override
			protected URI term(Statement st) {
				return st.getPredicate();
			}

			@Override
			protected void removeIteration(Iterator<Statement> iter, URI pred) {
				AbstractModel.this.removeTermIteration(iter, null, pred, null);
			}
		};
	}

	public Set<Value> objects() {
		return new ValueSet<Value>() {

			@Override
			public boolean contains(Object o) {
				if (o instanceof Value) {
					return AbstractModel.this.contains(null, null, (Value) o);
				}
				return false;
			}

			@Override
			public boolean remove(Object o) {
				if (o instanceof Value) {
					return AbstractModel.this.remove(null, null, (Value) o);
				}
				return false;
			}

			@Override
			public boolean add(Value obj) {
				return AbstractModel.this.add(null, null, obj);
			}

			@Override
			protected Value term(Statement st) {
				return st.getObject();
			}

			@Override
			protected void removeIteration(Iterator<Statement> iter, Value obj) {
				AbstractModel.this.removeTermIteration(iter, null, null, obj);
			}
		};
	}

	public Set<Resource> contexts() {
		return new ValueSet<Resource>() {

			@Override
			public boolean contains(Object o) {
				if (o instanceof Resource || o == null) {
					return AbstractModel.this.contains(null, null, null,
							(Resource) o);
				}
				return false;
			}

			@Override
			public boolean remove(Object o) {
				if (o instanceof Resource || o == null) {
					return AbstractModel.this.remove(null, null, null,
							(Resource) o);
				}
				return false;
			}

			@Override
			public boolean add(Resource context) {
				return AbstractModel.this.add(null, null, null, context);
			}

			@Override
			protected Resource term(Statement st) {
				return st.getContext();
			}

			@Override
			protected void removeIteration(Iterator<Statement> iter,
					Resource term) {
				AbstractModel.this
						.removeTermIteration(iter, null, null, null, term);
			}
		};
	}

	private abstract class ValueSet<V extends Value> extends AbstractSet<V> {

		private final class ValueSetIterator implements Iterator<V> {
			private final Iterator<Statement> iter;
			private final Set<V> set = new LinkedHashSet<V>();
			private Statement current;
			private Statement next;

			private ValueSetIterator(Iterator<Statement> iter) {
				this.iter = iter;
			}

			public boolean hasNext() {
				if (next == null) {
					next = findNext();
				}
				return next != null;
			}

			public V next() {
				if (next == null) {
					next = findNext();
					if (next == null) {
						throw new NoSuchElementException();
					}
				}
				current = next;
				next = null;
				V value = term(current);
				set.add(value);
				return value;
			}

			public void remove() {
				if (current == null) {
					throw new IllegalStateException();
				}
				removeIteration(iter, term(current));
				current = null;
			}

			private Statement findNext() {
				while (iter.hasNext()) {
					Statement st = iter.next();
					if (accept(st)) {
						return st;
					}
				}
				return null;
			}

			private boolean accept(Statement st) {
				return !set.contains(term(st));
			}
		}

		@Override
		public Iterator<V> iterator() {
			return new ValueSetIterator(AbstractModel.this.iterator());
		}

		@Override
		public void clear() {
			AbstractModel.this.clear();
		}

		@Override
		public boolean isEmpty() {
			return AbstractModel.this.isEmpty();
		}

		@Override
		public int size() {
			Iterator<Statement> iter = AbstractModel.this.iterator();
			try {
				Set<V> set = new LinkedHashSet<V>();
				while (iter.hasNext()) {
					set.add(term(iter.next()));
				}
				return set.size();
			} finally {
				AbstractModel.this.closeIterator(iter);
			}
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			boolean modified = false;
			Iterator<?> i = c.iterator();
			try {
			    while (i.hasNext())
			        modified |= remove(i.next());
			} finally {
				closeIterator(c, i);
			}
			return modified;
		}

		@Override
		public Object[] toArray() {
			Iterator<Statement> iter = AbstractModel.this.iterator();
			try {
				Set<V> set = new LinkedHashSet<V>();
				while (iter.hasNext()) {
					set.add(term(iter.next()));
				}
				return set.toArray();
			} finally {
				AbstractModel.this.closeIterator(iter);
			}
		}

		@Override
		public <T> T[] toArray(T[] a) {
			Iterator<Statement> iter = AbstractModel.this.iterator();
			try {
				Set<V> set = new LinkedHashSet<V>();
				while (iter.hasNext()) {
					set.add(term(iter.next()));
				}
				return set.toArray(a);
			} finally {
				AbstractModel.this.closeIterator(iter);
			}
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			Iterator<?> e = c.iterator();
			try {
				while (e.hasNext())
				    if (!contains(e.next()))
				        return false;
				return true;
			} finally {
				closeIterator(c, e);
			}
		}

		@Override
		public boolean addAll(Collection<? extends V> c) {
			Iterator<? extends V> e = c.iterator();
			try {
				boolean modified = false;
				while (e.hasNext()) {
				    if (add(e.next()))
				        modified = true;
				}
				return modified;
			} finally {
				closeIterator(c, e);
			}
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			Iterator<V> e = iterator();
			try {
				boolean modified = false;
				while (e.hasNext()) {
				    if (!c.contains(e.next())) {
				        e.remove();
				        modified = true;
				    }
				}
				return modified;
			} finally {
				closeIterator(e);
			}
		}

		@Override
		public abstract boolean add(V term);

		protected abstract V term(Statement st);

		protected abstract void removeIteration(Iterator<Statement> iter, V term);

		protected void closeIterator(Iterator<?> iter) {
			AbstractModel.this.closeIterator(((ValueSetIterator) iter).iter);
		}

		private void closeIterator(Collection<?> c, Iterator<?> e) {
			if (c instanceof AbstractModel) {
				((AbstractModel) c).closeIterator(e);
			} else if (c instanceof ValueSet) {
				((ValueSet<?>) c).closeIterator(e);
			}
		}
	}

	/**
	 * Called by aggregate sets when a term has been removed from a term
	 * iterator. Exactly one of the last four terms will be non-empty.
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
	public abstract void removeTermIteration(Iterator<Statement> iter,
			Resource subj, URI pred, Value obj, Resource... contexts);

	/**
	 * Cleans up any resources used by this iterator. After this call the given
	 * iterator should not be used.
	 * 
	 * @param iter Iterator to clean up
	 */
	protected void closeIterator(Iterator<?> iter) {
		if (iter instanceof ValueSet.ValueSetIterator) {
			closeIterator(((ValueSet.ValueSetIterator) iter).iter);
		}
	}

	private void closeIterator(Collection<?> c, Iterator<?> e) {
		if (c instanceof AbstractModel) {
			((AbstractModel) c).closeIterator(e);
		} else if (c instanceof ValueSet) {
			((ValueSet<?>) c).closeIterator(e);
		}
	}

}
