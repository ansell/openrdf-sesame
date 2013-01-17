/*
 * Copyright (c) 2012-2013 3 Round Stones Inc., Some rights reserved.
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.util.PatternIterator;
import org.openrdf.model.util.LexicalValueComparator;

/**
 * {@link Model} implementation using {@link TreeSet}.
 * 
 * @author James Leigh
 */
public class TreeModel extends AbstractModel implements SortedSet<Statement> {
	private static final long serialVersionUID = 7893197431354524479L;
	static final Resource[] NULL_CTX = new Resource[] { null };
	static final URI BEFORE = new URIImpl("urn:from");
	static final URI AFTER = new URIImpl("urn:to");
	private final LexicalValueComparator vc = new LexicalValueComparator();
	final Map<String, String> namespaces = new TreeMap<String, String>();
	final List<StatementTree> trees = new ArrayList<StatementTree>();

	public TreeModel() {
		trees.add(new StatementTree("spog".toCharArray()));
	}

	public TreeModel(Model model) {
		this(model.getNamespaces());
		addAll(model);
	}

	public TreeModel(Collection<? extends Statement> c) {
		this();
		addAll(c);
	}

	public TreeModel(Map<String, String> namespaces,
			Collection<? extends Statement> c) {
		this(c);
		this.namespaces.putAll(namespaces);
	}

	public TreeModel(Map<String, String> namespaces) {
		this();
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

	public String removeNamespace(String prefix) {
		return namespaces.remove(prefix);
	}

	@Override
	public int size() {
		return trees.get(0).size();
	}

	@Override
	public void clear() {
		for (StatementTree tree : trees) {
			tree.clear();
		}
	}

	public Comparator<? super Statement> comparator() {
		return trees.get(0).tree.comparator();
	}

	public Statement first() {
		return trees.get(0).tree.first();
	}

	public Statement last() {
		return trees.get(0).tree.last();
	}

	public Statement lower(Statement e) {
		return trees.get(0).tree.lower(e);
	}

	public Statement floor(Statement e) {
		return trees.get(0).tree.floor(e);
	}

	public Statement ceiling(Statement e) {
		return trees.get(0).tree.ceiling(e);
	}

	public Statement higher(Statement e) {
		return trees.get(0).tree.higher(e);
	}

	public Statement pollFirst() {
		try {
			Statement first = trees.get(0).tree.first();
			remove(first);
			return first;
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	public Statement pollLast() {
		try {
		Statement last = trees.get(0).tree.last();
			remove(last);
			return last;
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	public SortedSet<Statement> subSet(Statement fromElement,
			Statement toElement) {
		return subSet(fromElement, true, toElement, false);
	}

	public SortedSet<Statement> headSet(Statement toElement) {
		return subSet(before(null,null,null,null), true, toElement, false);
	}

	public SortedSet<Statement> tailSet(Statement fromElement) {
		return subSet(fromElement, true, after(null,null,null,null), true);
	}

	public boolean add(Resource subj, URI pred, Value obj, Resource... contexts) {
		if (subj == null || pred == null || obj == null)
			throw new UnsupportedOperationException("Incomplete statement");
		boolean changed = false;
		for (Value ctx : notEmpty(contexts)) {
			if (ctx == null || ctx instanceof Resource) {
				Statement st = new TreeStatement(subj, pred, obj,
						(Resource) ctx);
				for (StatementTree tree : trees) {
					changed |= tree.add(st);
				}
			}
		}
		return changed;
	}

	public boolean contains(Value subj, Value pred, Value obj,
			Value... contexts) {
		if (contexts == null || contexts.length == 1 && contexts[0] == null) {
			Iterator<Statement> iter = match(subj, pred, obj, null);
			while (iter.hasNext()) {
				if (iter.next().getContext() == null)
					return true;
			}
			return false;
		} else if (contexts.length == 0) {
			return match(subj, pred, obj, null).hasNext();
		} else {
			for (Value ctx : contexts) {
				if (ctx == null) {
					if (contains(subj, pred, obj, (Resource[]) null))
						return true;
				} else if (match(subj, pred, obj, ctx).hasNext()) {
					return true;
				}
			}
			return false;
		}
	}

	public boolean remove(Value subj, Value pred, Value obj, Value... contexts) {
		boolean changed = false;
		if (contexts == null || contexts.length == 1 && contexts[0] == null) {
			Iterator<Statement> iter = match(subj, pred, obj, null);
			while (iter.hasNext()) {
				if (iter.next().getContext() == null) {
					iter.remove();
					changed = true;
				}
			}
		} else if (contexts.length == 0) {
			Iterator<Statement> iter = match(subj, pred, obj, null);
			while (iter.hasNext()) {
				iter.next();
				iter.remove();
				changed = true;
			}
		} else {
			for (Value ctx : contexts) {
				if (ctx == null) {
					changed |= remove(subj, pred, obj, (Resource[]) null);
				} else {
					Iterator<Statement> iter = match(subj, pred, obj, ctx);
					while (iter.hasNext()) {
						iter.next();
						iter.remove();
						changed = true;
					}
				}
			}
		}
		return changed;
	}

	@Override
	public Iterator<Statement> iterator() {
		return match(null, null, null, null);
	}

	public Model filter(final Value subj, final Value pred, final Value obj,
			final Value... contexts) {
		if (contexts != null && contexts.length == 0) {
			return new FilteredModel(this, subj, pred, obj, contexts) {
				private static final long serialVersionUID = 396293781006255959L;

				@Override
				public Iterator<Statement> iterator() {
					return match(subj, pred, obj, null);
				}

				protected void removeFilteredTermIteration(Iterator<Statement> iter,
						Resource subj, URI pred, Value obj, Resource... contexts) {
					TreeModel.this.removeTermIteration(iter, subj, pred, obj, contexts);
				}
			};
		} else if (contexts != null && contexts.length == 1 && contexts[0] != null) {
			return new FilteredModel(this, subj, pred, obj, contexts) {
				private static final long serialVersionUID = 396293781006255959L;

				@Override
				public Iterator<Statement> iterator() {
					return match(subj, pred, obj, contexts[0]);
				}

				protected void removeFilteredTermIteration(Iterator<Statement> iter,
						Resource subj, URI pred, Value obj, Resource... contexts) {
					TreeModel.this.removeTermIteration(iter, subj, pred, obj, contexts);
				}
			};
		} else {
			return new FilteredModel(this, subj, pred, obj, contexts) {
				private static final long serialVersionUID = 396293781006255959L;

				@Override
				public Iterator<Statement> iterator() {
					 return new PatternIterator<Statement>(match(subj, pred,
							obj, null), subj, pred, obj, contexts);
				}

				protected void removeFilteredTermIteration(Iterator<Statement> iter,
						Resource subj, URI pred, Value obj, Resource... contexts) {
					TreeModel.this.removeTermIteration(iter, subj, pred, obj, contexts);
				}
			};
		}
	}

	@Override
	public void removeTermIteration(Iterator<Statement> iterator, Resource subj,
			URI pred, Value obj, Resource... contexts) {
		TreeSet<Statement> owner = ((ModelIterator) iterator).getOwner();
		if (contexts == null || contexts.length == 1 && contexts[0] == null) {
			StatementTree chosen = choose(subj, pred, obj, null);
			Iterator<Statement> iter = chosen
					.subIterator(before(subj, pred, obj, null),true,
							after(subj, pred, obj, null),true);
			iter = new PatternIterator<Statement>(iter, subj, pred, obj,
					contexts);
			removeAll(owner, chosen, iter);
		} else if (contexts.length == 0) {
			StatementTree chosen = choose(subj, pred, obj, null);
			Iterator<Statement> iter = chosen
					.subIterator(before(subj, pred, obj, null),true,
							after(subj, pred, obj, null),true);
			removeAll(owner, chosen, iter);
		} else {
			for (Value ctx : notEmpty(contexts)) {
				if (ctx == null) {
					removeTermIteration(iterator, subj, pred, obj,
							(Resource[]) null);
				} else {
					StatementTree chosen = choose(subj, pred, obj, ctx);
					Iterator<Statement> iter = chosen.subIterator(
							before(subj, pred, obj, ctx),true,
							after(subj, pred, obj, ctx),true);
					removeAll(owner, chosen, iter);
				}
			}
		}
	}

	Iterator<Statement> match(Value subj, Value pred, Value obj,
			Value ctx) {
		if (!isResourceURIResource(subj, pred, ctx)) {
			Set<Statement> emptySet = Collections.emptySet();
			return emptySet.iterator();
		}
		StatementTree tree = choose(subj, pred, obj, ctx);
		Iterator<Statement> it = tree.subIterator(before(subj, pred, obj, ctx),true,
				after(subj, pred, obj, ctx),true);
		return new ModelIterator(it, tree);
	}

	int compareValue(Value o1, Value o2) {
		if (o1 == o2)
			return 0;
		if (o1 == BEFORE)
			return -1;
		if (o2 == BEFORE)
			return 1;
		if (o1 == AFTER)
			return 1;
		if (o2 == AFTER)
			return -1;
		return vc.compare(o1, o2);
	}

	SortedSet<Statement> subSet(Statement lo,
			boolean loInclusive, Statement hi, boolean hiInclusive) {
		return new SubSet(this, new TreeStatement(lo), loInclusive, new TreeStatement(hi), hiInclusive);
	}

	private void removeAll(TreeSet<Statement> owner, StatementTree chosen,
			Iterator<Statement> iter) {
		while (iter.hasNext()) {
			Statement last = iter.next();
			for (StatementTree tree : trees) {
				if (tree.owns(owner)) {
					tree.reindex();
					tree.remove(last);
				} else if (tree != chosen) {
					tree.remove(last);
				}
			}
			iter.remove(); // remove from chosen
		}
	}

	private boolean isResourceURIResource(Value subj, Value pred, Value ctx) {
		return (subj == null || subj instanceof Resource)
				&& (pred == null || pred instanceof URI)
				&& (ctx == null || ctx instanceof Resource);
	}

	private Value[] notEmpty(Value[] contexts) {
		if (contexts == null || contexts.length == 0)
			return new Resource[] { null };
		return contexts;
	}

	private Statement before(Value subj, Value pred, Value obj, Value ctx) {
		Resource s = subj instanceof Resource ? (Resource) subj : BEFORE;
		URI p = pred instanceof URI ? (URI) pred : BEFORE;
		Value o = obj instanceof Value ? obj : BEFORE;
		Resource c = ctx instanceof Resource ? (Resource) ctx : BEFORE;
		return new TreeStatement(s, p, o, c);
	}

	private Statement after(Value subj, Value pred, Value obj, Value ctx) {
		Resource s = subj instanceof Resource ? (Resource) subj : AFTER;
		URI p = pred instanceof URI ? (URI) pred : AFTER;
		Value o = obj instanceof Value ? obj : AFTER;
		Resource c = ctx instanceof Resource ? (Resource) ctx : AFTER;
		return new TreeStatement(s, p, o, c);
	}

	private StatementTree choose(Value subj, Value pred, Value obj, Value ctx) {
		for (StatementTree tree : trees) {
			if (tree.isIndexed(subj, pred, obj, ctx))
				return tree;
		}
		return index(subj, pred, obj, ctx);
	}

	private StatementTree index(Value subj, Value pred, Value obj, Value ctx) {
		int idx = 0;
		char[] index = new char[4];
		if (subj != null) {
			index[idx++] = 's';
		}
		if (pred != null) {
			index[idx++] = 'p';
		}
		if (obj != null) {
			index[idx++] = 'o';
		}
		if (ctx != null) {
			index[idx++] = 'g';
		}
		if (pred == null) {
			index[idx++] = 'p';
		}
		if (obj == null) {
			index[idx++] = 'o';
		}
		if (ctx == null) {
			index[idx++] = 'g';
		}
		if (subj == null) {
			index[idx++] = 's';
		}
		StatementTree tree = new StatementTree(index);
		tree.addAll(trees.get(0));
		trees.add(tree);
		return tree;
	}

	private class ModelIterator implements Iterator<Statement> {

		private Iterator<Statement> iter;

		private TreeSet<Statement> owner;

		private Statement last;

		public ModelIterator(Iterator<Statement> iter, StatementTree owner) {
			this.iter = iter;
			this.owner = owner.tree;
		}

		public TreeSet<Statement> getOwner() {
			return owner;
		}

		public boolean hasNext() {
			return iter.hasNext();
		}

		public Statement next() {
			return last = iter.next();
		}

		public void remove() {
			if (last == null) {
				throw new IllegalStateException();
			}
			for (StatementTree tree : trees) {
				removeFrom(tree);
			}
			iter.remove(); // remove from owner
		}

		private void removeFrom(StatementTree subjects) {
			if (!subjects.owns(owner)) {
				subjects.remove(last);
			}
		}
	}

	static class TreeStatement extends ContextStatementImpl {
		private static final long serialVersionUID = -7720419322256724495L;

		public TreeStatement(Statement st) {
			super(st.getSubject(),st.getPredicate(),st.getObject(),st.getContext());
		}

		public TreeStatement(Resource subject, URI predicate, Value object,
				Resource ctx) {
			super(subject, predicate, object, ctx);
		}
	}

	class StatementTree {
		private final char[] index;
		TreeSet<Statement> tree;

		public StatementTree(char[] index) {
			this.index = index;
			Comparator<Statement>[] comparators = new Comparator[index.length];
			for (int i = 0; i < index.length; i++) {
				switch (index[i]) {
				case 's':
					comparators[i] = new SubjectComparator();
					break;
				case 'p':
					comparators[i] = new PredicateComparator();
					break;
				case 'o':
					comparators[i] = new ObjectComparator();
					break;
				case 'g':
					comparators[i] = new GraphComparator();
					break;
				default:
					throw new AssertionError();
				}
			}
			tree = new TreeSet<Statement>(new StatementComparator(comparators));
		}

		public boolean owns(TreeSet<Statement> set) {
			return tree == set;
		}

		public boolean isIndexed(Value subj, Value pred, Value obj, Value ctx) {
			boolean wild = false;
			for (int i = 0; i < index.length; i++) {
				switch (index[i]) {
				case 's':
					if (subj == null)
						wild = true;
					else if (wild)
						return false;
					break;
				case 'p':
					if (pred == null)
						wild = true;
					else if (wild)
						return false;
					break;
				case 'o':
					if (obj == null)
						wild = true;
					else if (wild)
						return false;
					break;
				case 'g':
					if (ctx == null)
						wild = true;
					else if (wild)
						return false;
					break;
				default:
					throw new AssertionError();
				}
			}
			return true;
		}

		public void reindex() {
			TreeSet<Statement> treeSet = new TreeSet<Statement>(
					tree.comparator());
			treeSet.addAll(tree);
			tree = treeSet;
		}

		public boolean add(Statement e) {
			return tree.add(e);
		}

		public boolean addAll(StatementTree c) {
			return tree.addAll(c.tree);
		}

		public int size() {
			return tree.size();
		}

		public void clear() {
			tree.clear();
		}

		public boolean remove(Object o) {
			return tree.remove(o);
		}

		public Iterator<Statement> subIterator(Statement fromElement, boolean fromInclusive,
				Statement toElement, boolean toInclusive) {
			return tree.subSet(fromElement, true, toElement, true).iterator();
		}
	}

	class SubjectComparator implements Comparator<Statement> {
		public int compare(Statement s1, Statement s2) {
			return compareValue(s1.getSubject(), s2.getSubject());
		}
	}

	class PredicateComparator implements Comparator<Statement> {
		public int compare(Statement s1, Statement s2) {
			return compareValue(s1.getPredicate(), s2.getPredicate());
		}
	}

	class ObjectComparator implements Comparator<Statement> {
		public int compare(Statement s1, Statement s2) {
			return compareValue(s1.getObject(), s2.getObject());
		}
	}

	class GraphComparator implements Comparator<Statement> {
		public int compare(Statement s1, Statement s2) {
			return compareValue(s1.getContext(), s2.getContext());
		}
	}

	static class StatementComparator implements Comparator<Statement> {
		private final Comparator<Statement>[] comparators;

		public StatementComparator(Comparator<Statement>... comparators) {
			this.comparators = comparators;
		}

		public int compare(Statement s1, Statement s2) {
			for (Comparator<Statement> c : comparators) {
				int r1 = c.compare(s1, s2);
				if (r1 != 0)
					return r1;
			}
			return 0;
		}
	}

	static class SubSet extends AbstractSet<Statement> implements SortedSet<Statement> {
		private final TreeModel model;
        private final TreeStatement lo, hi;
        private final boolean loInclusive, hiInclusive;

		public SubSet(TreeModel model, TreeStatement lo, boolean loInclusive,
				TreeStatement hi, boolean hiInclusive) {
			this.model = model;
			this.lo = lo;
			this.loInclusive = loInclusive;
			this.hi = hi;
			this.hiInclusive = hiInclusive;
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

		public int size() {
			return subSet().size();
		}

		public void clear() {
			StatementTree tree = model.trees.get(0);
			Iterator<Statement> it = tree.subIterator(lo, loInclusive, hi, hiInclusive);
			it = model.new ModelIterator(it, tree);
			while (it.hasNext()) {
				it.remove();
			}
		}

		public Comparator<? super Statement> comparator() {
			return model.comparator();
		}

		public Statement first() {
			return subSet().first();
		}

		public Statement last() {
			return subSet().last();
		}

		public Statement lower(Statement e) {
			return subSet().lower(e);
		}

		public boolean isEmpty() {
			return subSet().isEmpty();
		}

		public Statement floor(Statement e) {
			return subSet().floor(e);
		}

		public Statement ceiling(Statement e) {
			return subSet().ceiling(e);
		}

		public Statement higher(Statement e) {
			return subSet().higher(e);
		}

		public Statement pollFirst() {
			try {
				Statement first = subSet().first();
				model.remove(first);
				return first;
			} catch (NoSuchElementException e) {
				return null;
			}
		}

		public Statement pollLast() {
			try {
				Statement last = subSet().last();
				model.remove(last);
				return last;
			} catch (NoSuchElementException e) {
				return null;
			}
		}

		public SortedSet<Statement> subSet(Statement fromElement,
				Statement toElement) {
			boolean fromInclusive = true;
					boolean toInclusive = false;
			if (comparator().compare(fromElement, lo) < 0) {
				fromElement = lo;
				fromInclusive = loInclusive;
			}
			if (comparator().compare(hi, toElement) < 0) {
				toElement = hi;
				toInclusive = hiInclusive;
			}
			return model.subSet(fromElement, fromInclusive, toElement,
					toInclusive);
		}

		public SortedSet<Statement> headSet(Statement toElement) {
			boolean toInclusive = false;
			if (comparator().compare(hi, toElement) < 0) {
				toElement = hi;
				toInclusive = hiInclusive;
			}
			return model.subSet(lo, loInclusive, toElement,
					toInclusive);
		}

		public SortedSet<Statement> tailSet(Statement fromElement) {
			boolean fromInclusive = true;
			if (comparator().compare(fromElement, lo) < 0) {
				fromElement = lo;
				fromInclusive = loInclusive;
			}
			return model.subSet(fromElement, fromInclusive, hi,
					hiInclusive);
		}

		public Iterator<Statement> iterator() {
			StatementTree tree = model.trees.get(0);
			Iterator<Statement> it = tree.subIterator(lo, loInclusive, hi,
					hiInclusive);
			return model.new ModelIterator(it, tree);
		}

		private NavigableSet<Statement> subSet() {
			return model.trees.get(0).tree.subSet(lo, loInclusive, hi, hiInclusive);
		}
	}
}
