/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import org.openrdf.util.iterators.FilterIterator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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
 * {@link Model} implementation using {@link LinkedHashSet}.
 * <p>
 * <b>Note that this implementation is not synchronized.</b> If multiple threads
 * access a model concurrently, and at least one of the threads modifies the
 * model, it must be synchronized externally. This is typically accomplished by
 * synchronizing on some object that naturally encapsulates the model. If no
 * such object exists, the set should be "wrapped" using the
 * Collections.synchronizedSet method. This is best done at creation time, to
 * prevent accidental unsynchronized access to the LinkedHashModel instance
 * (though the synchronization guarantee is only when accessing via the Set
 * interface methods):
 * </p>
 * 
 * <pre>
 * Set<Statement> s = Collections.synchronizedSet(new LinkedHashModel(...));
 * </pre>
 * 
 * @author James Leigh
 */
@SuppressWarnings("unchecked")
public class LinkedHashModel extends AbstractSet<Statement> implements Model {

	private static final long serialVersionUID = -9161104123818983614L;

	static final Resource[] NULL_CTX = new Resource[] { null };

	Map<String, String> namespaces = new LinkedHashMap<String, String>();

	transient Map<Value, ModelNode<?>> values;

	transient Set<ModelStatement> statements;

	public LinkedHashModel() {
		super();
		values = new HashMap<Value, ModelNode<?>>();
		statements = new LinkedHashSet<ModelStatement>();
	}

	public LinkedHashModel(Collection<? extends Statement> c) {
		super();
		values = new HashMap<Value, ModelNode<?>>(c.size() * 2);
		statements = new LinkedHashSet<ModelStatement>(c.size());
		addAll(c);
	}

	public LinkedHashModel(int size) {
		super();
		values = new HashMap<Value, ModelNode<?>>(size * 2);
		statements = new LinkedHashSet<ModelStatement>(size);
	}

	public LinkedHashModel(Map<String, String> namespaces, Collection<? extends Statement> c) {
		this(c);
		this.namespaces.putAll(namespaces);
	}

	public LinkedHashModel(Map<String, String> namespaces) {
		this();
		this.namespaces.putAll(namespaces);
	}

	public LinkedHashModel(Map<String, String> namespaces, int size) {
		this(size);
		this.namespaces.putAll(namespaces);
	}

	public String getNamespace(String prefix) {
		return namespaces.get(prefix);
	}

	public Map<String, String> getNamespaces() {
		return namespaces;
	}

	public String setNamespace(String prefix, String name) {
		return namespaces.put(prefix, name);
	}

	public void removeNamespace(String prefix) {
		namespaces.remove(prefix);
	}

	@Override
	public int size() {
		return statements.size();
	}

	@Override
	public boolean add(Statement st) {
		return add(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
	}

	public boolean add(Resource subj, URI pred, Value obj, Resource... contexts) {
		Resource[] ctxs = notNull(contexts);
		if (ctxs.length == 0) {
			ctxs = NULL_CTX;
		}
		boolean changed = false;
		for (Resource ctx : ctxs) {
			ModelNode<Resource> s = asNode(subj);
			ModelNode<URI> p = asNode(pred);
			ModelNode<Value> o = asNode(obj);
			ModelNode<Resource> c = asNode(ctx);
			ModelStatement st = new ModelStatement(s, p, o, c);
			changed |= addModelStatement(st);
		}
		return changed;
	}

	@Override
	public void clear() {
		values.clear();
		statements.clear();
	}

	@Override
	public boolean remove(Object o) {
		if (o instanceof Statement) {
			Iterator<ModelStatement> iter = find((Statement)o);
			if (iter.hasNext()) {
				iter.next();
				iter.remove();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean contains(Object o) {
		if (o instanceof Statement) {
			return find((Statement)o).hasNext();
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Iterator iterator() {
		return match(null, null, null);
	}

	public boolean contains(Resource subj, URI pred, Value obj, Resource... contexts) {
		return match(subj, pred, obj, contexts).hasNext();
	}

	public boolean remove(Resource subj, URI pred, Value obj, Resource... contexts) {
		Iterator<ModelStatement> iter = match(subj, pred, obj, contexts);
		if (!iter.hasNext()) {
			return false;
		}
		while (iter.hasNext()) {
			iter.next();
			iter.remove();
		}
		return true;
	}

	public boolean clear(Resource... contexts) {
		return remove(null, null, null, contexts);
	}

	public Model filter(Resource subj, URI pred, Value obj, Resource... contexts) {
		return new FilteredModel(subj, pred, obj, contexts);
	}

	public Set<Resource> subjects() {
		return subjects(null, null);
	}

	public Set<URI> predicates() {
		return predicates(null, null);
	}

	public Set<Value> objects() {
		return objects(null, null);
	}

	public Set<Resource> contexts() {
		return contexts(null, null, null);
	}

	public Value objectValue()
		throws ModelException
	{
		Iterator<Value> iter = objects().iterator();
		if (iter.hasNext()) {
			Value obj = iter.next();
			if (iter.hasNext()) {
				throw new ModelException();
			}
			return obj;
		}
		return null;
	}

	public Literal objectLiteral()
		throws ModelException
	{
		Value obj = objectValue();
		if (obj == null) {
			return null;
		}
		if (obj instanceof Literal) {
			return (Literal)obj;
		}
		throw new ModelException();
	}

	public Resource objectResource()
		throws ModelException
	{
		Value obj = objectValue();
		if (obj == null) {
			return null;
		}
		if (obj instanceof Resource) {
			return (Resource)obj;
		}
		throw new ModelException();
	}

	public URI objectURI()
		throws ModelException
	{
		Value obj = objectValue();
		if (obj == null) {
			return null;
		}
		if (obj instanceof URI) {
			return (URI)obj;
		}
		throw new ModelException();
	}

	public String objectString()
		throws ModelException
	{
		Value obj = objectValue();
		if (obj == null) {
			return null;
		}
		return obj.stringValue();
	}

	@Override
	public int hashCode() {
		return size();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof Model) {
			Model model = (Model)o;
			return ModelUtil.equals(this, model);
		}
		return false;
	}

	Set<Resource> contexts(final Resource subj, final URI pred, final Value obj) {
		return new ValueSet<Resource>() {

			@Override
			public boolean contains(Object o) {
				if (o instanceof Resource || o == null) {
					return LinkedHashModel.this.contains(subj, pred, obj, (Resource)o);
				}
				return false;
			}

			@Override
			public boolean remove(Object o) {
				if (o instanceof Resource || o == null) {
					return LinkedHashModel.this.remove(subj, pred, obj, (Resource)o);
				}
				return false;
			}

			@Override
			public boolean add(Resource ctx) {
				if (subj == null || pred == null || obj == null) {
					throw new UnsupportedOperationException("Incomplete statement");
				}
				if (contains(ctx)) {
					return false;
				}
				return LinkedHashModel.this.add(subj, pred, obj, ctx);
			}

			@Override
			public void clear() {
				LinkedHashModel.this.remove(subj, pred, obj);
			}

			@Override
			protected ModelIterator statementIterator() {
				return match(subj, pred, obj);
			}

			@Override
			protected ModelNode<Resource> node(ModelStatement st) {
				return st.ctx;
			}

			@Override
			protected Set<ModelStatement> set(ModelNode<Resource> node) {
				return node.contexts;
			}
		};
	}

	Set<Value> objects(final Resource subj, final URI pred, final Resource... contexts) {
		return new ValueSet<Value>() {

			@Override
			public boolean contains(Object o) {
				if (o instanceof Value) {
					return LinkedHashModel.this.contains(subj, pred, (Value)o, contexts);
				}
				return false;
			}

			@Override
			public boolean remove(Object o) {
				if (o instanceof Value) {
					return LinkedHashModel.this.remove(subj, pred, (Value)o, contexts);
				}
				return false;
			}

			@Override
			public boolean add(Value obj) {
				if (subj == null || pred == null) {
					throw new UnsupportedOperationException("Incomplete statement");
				}
				if (contains(obj)) {
					return false;
				}
				return LinkedHashModel.this.add(subj, pred, obj, contexts);
			}

			@Override
			public void clear() {
				LinkedHashModel.this.remove(subj, pred, null, contexts);
			}

			@Override
			protected ModelIterator statementIterator() {
				return match(subj, pred, null, contexts);
			}

			@Override
			protected ModelNode<Value> node(ModelStatement st) {
				return st.obj;
			}

			@Override
			protected Set<ModelStatement> set(ModelNode<Value> node) {
				return node.objects;
			}
		};
	}

	Set<URI> predicates(final Resource subj, final Value obj, final Resource... contexts) {
		return new ValueSet<URI>() {

			@Override
			public boolean contains(Object o) {
				if (o instanceof URI) {
					return LinkedHashModel.this.contains(subj, (URI)o, obj, contexts);
				}
				return false;
			}

			@Override
			public boolean remove(Object o) {
				if (o instanceof URI) {
					return LinkedHashModel.this.remove(subj, (URI)o, obj, contexts);
				}
				return false;
			}

			@Override
			public boolean add(URI pred) {
				if (subj == null || obj == null) {
					throw new UnsupportedOperationException("Incomplete statement");
				}
				if (contains(pred)) {
					return false;
				}
				return LinkedHashModel.this.add(subj, pred, obj, contexts);
			}

			@Override
			public void clear() {
				LinkedHashModel.this.remove(subj, null, obj, contexts);
			}

			@Override
			protected ModelIterator statementIterator() {
				return match(subj, null, obj, contexts);
			}

			@Override
			protected ModelNode<URI> node(ModelStatement st) {
				return st.pred;
			}

			@Override
			protected Set<ModelStatement> set(ModelNode<URI> node) {
				return node.predicates;
			}
		};
	}

	Set<Resource> subjects(final URI pred, final Value obj, final Resource... contexts) {
		return new ValueSet<Resource>() {

			@Override
			public boolean contains(Object o) {
				if (o instanceof Resource) {
					return LinkedHashModel.this.contains((Resource)o, pred, obj, contexts);
				}
				return false;
			}

			@Override
			public boolean remove(Object o) {
				if (o instanceof Resource) {
					return LinkedHashModel.this.remove((Resource)o, pred, obj, contexts);
				}
				return false;
			}

			@Override
			public boolean add(Resource subj) {
				if (pred == null || obj == null) {
					throw new UnsupportedOperationException("Incomplete statement");
				}
				if (contains(subj)) {
					return false;
				}
				return LinkedHashModel.this.add(subj, pred, obj, contexts);
			}

			@Override
			public void clear() {
				LinkedHashModel.this.remove(null, pred, obj, contexts);
			}

			@Override
			protected ModelIterator statementIterator() {
				return match(null, pred, obj, contexts);
			}

			@Override
			protected ModelNode<Resource> node(ModelStatement st) {
				return st.subj;
			}

			@Override
			protected Set<ModelStatement> set(ModelNode<Resource> node) {
				return node.subjects;
			}
		};
	}

	ModelIterator match(Resource subj, URI pred, Value obj, Resource... contexts) {
		assert contexts != null;
		Set<ModelStatement> s = null;
		Set<ModelStatement> p = null;
		Set<ModelStatement> o = null;
		if (subj != null) {
			if (!values.containsKey(subj))
				return emptyModelIterator();
			s = values.get(subj).subjects;
		}
		if (pred != null) {
			if (!values.containsKey(pred))
				return emptyModelIterator();
			p = values.get(pred).predicates;
		}
		if (obj != null) {
			if (!values.containsKey(obj))
				return emptyModelIterator();
			o = values.get(obj).objects;
		}
		Set<ModelStatement> set;
		contexts = notNull(contexts);
		if (contexts.length == 1) {
			if (!values.containsKey(contexts[0]))
				return emptyModelIterator();
			Set<ModelStatement> c = values.get(contexts[0]).contexts;
			set = smallest(statements, s, p, o, c);
		}
		else {
			set = smallest(statements, s, p, o);
		}
		Iterator<ModelStatement> it = set.iterator();
		Iterator<ModelStatement> iter;
		iter = new PatternIterator<ModelStatement>(it, subj, pred, obj, contexts);
		return new ModelIterator(iter, set);
	}

	boolean matches(Statement st, Resource subj, URI pred, Value obj, Resource... contexts) {
		if (subj != null && !subj.equals(st.getSubject())) {
			return false;
		}
		if (pred != null && !pred.equals(st.getPredicate())) {
			return false;
		}
		if (obj != null && !obj.equals(st.getObject())) {
			return false;
		}

		return matches(st.getContext(), contexts);
	}

	boolean matches(Resource[] stContext, Resource... contexts) {
		if (stContext != null && stContext.length > 0) {
			for (Resource c : stContext) {
				if (!matches(c, contexts)) {
					return false;
				}
			}
		}
		return true;
	}

	boolean matches(Resource stContext, Resource... contexts) {
		if (contexts != null && contexts.length == 0) {
			// Any context matches
			return true;
		}
		else {
			// Accept if one of the contexts from the pattern matches
			for (Resource context : notNull(contexts)) {
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

	Model emptyModel = new EmptyModel();

	Model emptyModel() {
		return emptyModel;
	}

	ModelIterator emptyModelIterator() {
		Set<ModelStatement> set = Collections.emptySet();
		return new ModelIterator(set.iterator(), set);
	}

	private class EmptyModel extends AbstractSet<Statement> implements Model {

		private static final long serialVersionUID = 3123007631452759092L;

		private Set<Statement> emptySet = Collections.emptySet();

		public String getNamespace(String prefix) {
			return namespaces.get(prefix);
		}

		public Map<String, String> getNamespaces() {
			return namespaces;
		}

		public String setNamespace(String prefix, String name) {
			return namespaces.put(prefix, name);
		}

		public void removeNamespace(String prefix) {
			namespaces.remove(prefix);
		}

		@Override
		public Iterator<Statement> iterator() {
			return emptySet.iterator();
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public boolean add(Statement e) {
			throw new UnsupportedOperationException("All statements are filtered out of view");
		}

		public boolean add(Resource subj, URI pred, Value obj, Resource... contexts) {
			throw new UnsupportedOperationException("All statements are filtered out of view");
		}

		public boolean clear(Resource... context) {
			return false;
		}

		public boolean contains(Resource subj, URI pred, Value obj, Resource... contexts) {
			return false;
		}

		public Set<Resource> contexts() {
			return Collections.emptySet();
		}

		public Model filter(Resource subj, URI pred, Value obj, Resource... contexts) {
			return emptyModel;
		}

		public Set<Value> objects() {
			return Collections.emptySet();
		}

		public Set<URI> predicates() {
			return Collections.emptySet();
		}

		public boolean remove(Resource subj, URI pred, Value obj, Resource... contexts) {
			return false;
		}

		public Set<Resource> subjects() {
			return Collections.emptySet();
		}

		public Literal objectLiteral() {
			return null;
		}

		public Value objectValue() {
			return null;
		}

		public Resource objectResource() {
			return null;
		}

		public URI objectURI() {
			return null;
		}

		public String objectString()
			throws ModelException
		{
			return null;
		}

		@Override
		public int hashCode() {
			return size();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o instanceof Model) {
				Model model = (Model)o;
				return model.isEmpty();
			}
			return false;
		}

	}

	private class FilteredModel extends AbstractSet<Statement> implements Model {

		private static final long serialVersionUID = -2353344619836326934L;

		private Resource subj;

		private URI pred;

		private Value obj;

		private Resource[] contexts;

		public FilteredModel(Resource subj, URI pred, Value obj, Resource... contexts) {
			this.subj = subj;
			this.pred = pred;
			this.obj = obj;
			this.contexts = notNull(contexts);
		}

		public String getNamespace(String prefix) {
			return namespaces.get(prefix);
		}

		public Map<String, String> getNamespaces() {
			return namespaces;
		}

		public String setNamespace(String prefix, String name) {
			return namespaces.put(prefix, name);
		}

		public void removeNamespace(String prefix) {
			namespaces.remove(prefix);
		}

		@Override
		public Iterator<Statement> iterator() {
			final ModelIterator iter = statementIterator();
			return new Iterator<Statement>() {

				private ModelStatement current;

				private ModelStatement next;

				public boolean hasNext() {
					if (next == null && iter.hasNext()) {
						next = iter.next();
					}
					return next != null;
				}

				public ModelStatement next() {
					if (next == null) {
						next = iter.next();
					}
					current = next;
					next = null;
					return current;
				}

				public void remove() {
					iter.remove();
				}
			};
		}

		@Override
		public int size() {
			int size = 0;
			Iterator<ModelStatement> iter = statementIterator();
			while (iter.hasNext()) {
				size++;
				iter.next();
			}
			return size;
		}

		@Override
		public boolean contains(Object o) {
			if (o instanceof Statement) {
				Statement st = (Statement)o;
				if (accept(st)) {
					return LinkedHashModel.this.contains(o);
				}
			}
			return false;
		}

		@Override
		public boolean add(Statement st) {
			if (accept(st)) {
				return LinkedHashModel.this.add(st);
			}
			throw new IllegalArgumentException("Statement is filtered out of view: " + st);
		}

		public boolean add(Resource s, URI p, Value o, Resource... c) {
			if (!accept(s, p, o, c)) {
				throw new IllegalArgumentException("Statement is filtered out of view");
			}
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
			return LinkedHashModel.this.add(s, p, o, c);
		}

		@Override
		public void clear() {
			LinkedHashModel.this.remove(subj, pred, obj, contexts);
		}

		public boolean clear(Resource... c) {
			c = notNull(c);
			if (c.length == 0) {
				return remove(subj, pred, obj, contexts);
			}
			else if (matches(c, contexts)) {
				return LinkedHashModel.this.remove(subj, pred, obj, c);
			}
			else {
				return false;
			}
		}

		public boolean remove(Resource s, URI p, Value o, Resource... c) {
			if (!accept(s, p, o, c)) {
				return false;
			}
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
			return LinkedHashModel.this.remove(s, p, o, c);
		}

		public boolean contains(Resource s, URI p, Value o, Resource... c) {
			if (!accept(s, p, o, c)) {
				return false;
			}
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
			return LinkedHashModel.this.contains(s, p, o, c);
		}

		public Model filter(Resource s, URI p, Value o, Resource... c) {
			if (!accept(s, p, o, c)) {
				return emptyModel();
			}
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
			return LinkedHashModel.this.filter(s, p, o, c);
		}

		public Set<Resource> contexts() {
			if (contexts != null && contexts.length > 0) {
				return unmodifiableSet(new LinkedHashSet<Resource>(asList(contexts)));
			}
			return LinkedHashModel.this.contexts(subj, pred, obj);
		}

		public Set<Value> objects() {
			if (obj != null) {
				return Collections.singleton(obj);
			}
			return LinkedHashModel.this.objects(subj, pred, contexts);
		}

		public Set<URI> predicates() {
			if (pred != null) {
				return Collections.singleton(pred);
			}
			return LinkedHashModel.this.predicates(subj, obj, contexts);
		}

		public Set<Resource> subjects() {
			if (subj != null) {
				return Collections.singleton(subj);
			}
			return LinkedHashModel.this.subjects(pred, obj, contexts);
		}

		public Value objectValue()
			throws ModelException
		{
			Iterator<Value> iter = objects().iterator();
			if (iter.hasNext()) {
				Value obj = iter.next();
				if (iter.hasNext()) {
					throw new ModelException();
				}
				return obj;
			}
			return null;
		}

		public Literal objectLiteral()
			throws ModelException
		{
			Value obj = objectValue();
			if (obj == null) {
				return null;
			}
			if (obj instanceof Literal) {
				return (Literal)obj;
			}
			throw new ModelException();
		}

		public Resource objectResource()
			throws ModelException
		{
			Value obj = objectValue();
			if (obj == null) {
				return null;
			}
			if (obj instanceof Resource) {
				return (Resource)obj;
			}
			throw new ModelException();
		}

		public URI objectURI()
			throws ModelException
		{
			Value obj = objectValue();
			if (obj == null) {
				return null;
			}
			if (obj instanceof URI) {
				return (URI)obj;
			}
			throw new ModelException();
		}

		public String objectString()
			throws ModelException
		{
			Value obj = objectValue();
			if (obj == null) {
				return null;
			}
			return obj.stringValue();
		}

		@Override
		public int hashCode() {
			return size();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o instanceof Model) {
				Model model = (Model)o;
				return ModelUtil.equals(this, model);
			}
			return false;
		}

		private ModelIterator statementIterator() {
			return match(subj, pred, obj, contexts);
		}

		private boolean accept(Statement st) {
			return matches(st, subj, pred, obj, contexts);
		}

		private boolean accept(Resource s, URI p, Value o, Resource... c) {
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
			return true;
		}
	}

	private abstract class ValueSet<V extends Value> extends AbstractSet<V> {

		@Override
		public Iterator<V> iterator() {
			final Set<V> set = new LinkedHashSet<V>();
			final ModelIterator iter = statementIterator();
			return new Iterator<V>() {

				private ModelStatement current;

				private ModelStatement next;

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
					V value = convert(current);
					set.add(value);
					return value;
				}

				public void remove() {
					if (current == null) {
						throw new IllegalStateException();
					}
					removeAll(set(node(current)), iter.getOwner());
					current = null;
				}

				private ModelStatement findNext() {
					while (iter.hasNext()) {
						ModelStatement st = iter.next();
						if (accept(st)) {
							return st;
						}
					}
					return null;
				}

				private boolean accept(ModelStatement st) {
					return !set.contains(convert(st));
				}

				private V convert(ModelStatement st) {
					return node(st).getValue();
				}
			};
		}

		@Override
		public boolean isEmpty() {
			return !statementIterator().hasNext();
		}

		@Override
		public int size() {
			Set<V> set = new LinkedHashSet<V>();
			Iterator<ModelStatement> iter = statementIterator();
			while (iter.hasNext()) {
				set.add(node(iter.next()).getValue());
			}
			return set.size();
		}

		@Override
		public boolean remove(Object o) {
			if (values.containsKey(o)) {
				return removeAll(set((ModelNode<V>)values.get(o)), null);
			}
			return false;
		}

		protected abstract ModelIterator statementIterator();

		protected abstract ModelNode<V> node(ModelStatement st);

		protected abstract Set<ModelStatement> set(ModelNode<V> node);

		boolean removeAll(Set<ModelStatement> remove, Set<ModelStatement> owner) {
			if (remove.isEmpty()) {
				return false;
			}
			for (ModelStatement st : remove) {
				ModelNode<Resource> subj = st.subj;
				Set<ModelStatement> subjects = subj.subjects;
				if (subjects == owner) {
					subj.subjects = new LinkedHashSet<ModelStatement>(owner);
					subj.subjects.removeAll(remove);
				}
				else if (subjects != remove) {
					subjects.remove(st);
				}
				ModelNode<URI> pred = st.pred;
				Set<ModelStatement> predicates = pred.predicates;
				if (predicates == owner) {
					pred.predicates = new LinkedHashSet<ModelStatement>(owner);
					pred.predicates.removeAll(remove);
				}
				else if (predicates != remove) {
					predicates.remove(st);
				}
				ModelNode<Value> obj = st.obj;
				Set<ModelStatement> objects = obj.objects;
				if (objects == owner) {
					obj.objects = new LinkedHashSet<ModelStatement>(owner);
					obj.objects.removeAll(remove);
				}
				else if (objects != remove) {
					objects.remove(st);
				}
				ModelNode<Resource> ctx = st.ctx;
				Set<ModelStatement> contexts = ctx.contexts;
				if (contexts == owner) {
					ctx.contexts = new LinkedHashSet<ModelStatement>(owner);
					ctx.contexts.removeAll(remove);
				}
				else if (contexts != remove) {
					contexts.remove(st);
				}
				if (statements == owner) {
					statements = new LinkedHashSet<ModelStatement>(statements);
					statements.removeAll(remove);
				}
				else if (statements != remove && statements != owner) {
					statements.remove(st);
				}
			}
			remove.clear();
			return true;
		}
	}

	private class ModelIterator implements Iterator<ModelStatement> {

		private Iterator<ModelStatement> iter;

		private Set<ModelStatement> owner;

		private ModelStatement last;

		public ModelIterator(Iterator<ModelStatement> iter, Set<ModelStatement> owner) {
			this.iter = iter;
			this.owner = owner;
		}

		public Set<ModelStatement> getOwner() {
			return owner;
		}

		public boolean hasNext() {
			return iter.hasNext();
		}

		public ModelStatement next() {
			return last = iter.next();
		}

		public void remove() {
			if (last == null) {
				throw new IllegalStateException();
			}
			removeIfNotOwner(statements);
			removeIfNotOwner(last.subj.subjects);
			removeIfNotOwner(last.pred.predicates);
			removeIfNotOwner(last.obj.objects);
			removeIfNotOwner(last.ctx.contexts);
			iter.remove(); // remove from owner
		}

		private void removeIfNotOwner(Set<ModelStatement> subjects) {
			if (subjects != owner) {
				subjects.remove(last);
			}
		}
	}

	private class ModelNode<V extends Value> implements Serializable {

		private static final long serialVersionUID = -1205676084606998540L;

		Set<ModelStatement> subjects = new LinkedHashSet<ModelStatement>();

		Set<ModelStatement> predicates = new LinkedHashSet<ModelStatement>();

		Set<ModelStatement> objects = new LinkedHashSet<ModelStatement>();

		Set<ModelStatement> contexts = new LinkedHashSet<ModelStatement>();

		private V value;

		public ModelNode(V value) {
			this.value = value;
		}

		public V getValue() {
			return value;
		}
	}

	private class ModelStatement extends ContextStatementImpl {

		private static final long serialVersionUID = 2200404772364346279L;

		ModelNode<Resource> subj;

		ModelNode<URI> pred;

		ModelNode<Value> obj;

		ModelNode<Resource> ctx;

		public ModelStatement(ModelNode<Resource> subj, ModelNode<URI> pred, ModelNode<Value> obj,
				ModelNode<Resource> ctx)
		{
			super(subj.getValue(), pred.getValue(), obj.getValue(), ctx.getValue());
			assert subj != null;
			assert pred != null;
			assert obj != null;
			assert ctx != null;
			this.subj = subj;
			this.pred = pred;
			this.obj = obj;
			this.ctx = ctx;
		}

		public Resource getSubject() {
			return subj.getValue();
		}

		public URI getPredicate() {
			return pred.getValue();
		}

		public Value getObject() {
			return obj.getValue();
		}

		public Resource getContext() {
			return ctx.getValue();
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!super.equals(other))
				return false;
			if (getContext() == null)
				return ((Statement)other).getContext() == null;
			return getContext().equals(((Statement)other).getContext());
		}
	}

	private class PatternIterator<S extends Statement> extends FilterIterator<S> {

		private Resource subj;

		private URI pred;

		private Value obj;

		private Resource[] contexts;

		public PatternIterator(Iterator<S> iter, Resource subj, URI pred, Value obj, Resource... contexts) {
			super(iter);
			this.subj = subj;
			this.pred = pred;
			this.obj = obj;
			this.contexts = notNull(contexts);
		}

		@Override
		protected boolean accept(S st) {
			return matches(st, subj, pred, obj, contexts);
		}
	}

	private void writeObject(ObjectOutputStream s)
		throws IOException
	{
		// Write out any hidden serialization magic
		s.defaultWriteObject();
		// Write in size
		s.writeInt(statements.size());
		// Write in all elements
		for (ModelStatement st : statements) {
			Resource subj = st.getSubject();
			URI pred = st.getPredicate();
			Value obj = st.getObject();
			Resource ctx = st.getContext();
			s.writeObject(new ContextStatementImpl(subj, pred, obj, ctx));
		}
	}

	private Resource[] notNull(Resource[] contexts) {
		if (contexts == null) {
			return new Resource[] { null };
		}
		return contexts;
	}

	private void readObject(ObjectInputStream s)
		throws IOException, ClassNotFoundException
	{
		// Read in any hidden serialization magic
		s.defaultReadObject();
		// Read in size
		int size = s.readInt();
		values = new HashMap<Value, ModelNode<?>>(size * 2);
		statements = new LinkedHashSet<ModelStatement>(size);
		// Read in all elements
		for (int i = 0; i < size; i++) {
			Statement st = (Statement)s.readObject();
			add(st);
		}
	}

	private Iterator<ModelStatement> find(Statement st) {
		Resource subj = st.getSubject();
		URI pred = st.getPredicate();
		Value obj = st.getObject();
		Resource ctx = st.getContext();
		return match(subj, pred, obj, ctx);
	}

	private boolean addModelStatement(ModelStatement st) {
		Set<ModelStatement> subj = st.subj.subjects;
		Set<ModelStatement> pred = st.pred.predicates;
		Set<ModelStatement> obj = st.obj.objects;
		Set<ModelStatement> ctx = st.ctx.contexts;
		if (smallest(subj, pred, obj, ctx).contains(st)) {
			return false;
		}
		statements.add(st);
		subj.add(st);
		pred.add(st);
		obj.add(st);
		ctx.add(st);
		return true;
	}

	private Set<ModelStatement> smallest(Set<ModelStatement>... sets) {
		int minSize = Integer.MAX_VALUE;
		Set<ModelStatement> minSet = null;
		for (Set<ModelStatement> set : sets) {
			if (set != null && set.size() < minSize) {
				minSet = set;
			}
		}
		return minSet;
	}

	private <V extends Value> ModelNode<V> asNode(V value) {
		ModelNode<V> node = (ModelNode<V>)values.get(value);
		if (node != null)
			return node;
		node = new ModelNode<V>(value);
		values.put(value, node);
		return node;
	}
}
