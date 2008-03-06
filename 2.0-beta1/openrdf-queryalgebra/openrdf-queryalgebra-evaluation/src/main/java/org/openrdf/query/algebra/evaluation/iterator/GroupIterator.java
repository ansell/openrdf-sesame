/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIterationBase;
import info.aduna.lang.ObjectUtil;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.AggregateOperator;
import org.openrdf.query.algebra.Count;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.Max;
import org.openrdf.query.algebra.Min;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Group.AggregateBinding;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;

/**
 * @author David Huynh
 * @author Arjohn Kampman
 */
public class GroupIterator extends CloseableIterationBase<BindingSet, QueryEvaluationException> {

	private EvaluationStrategy _strategy;

	private BindingSet _bindings;

	private Group _group;

	private boolean _ordered;

	private Iterator<BindingSet> _iterator;

	public GroupIterator(EvaluationStrategy strategy, Group group, BindingSet bindings)
		throws QueryEvaluationException
	{
		_strategy = strategy;
		_group = group;
		_bindings = bindings;

		initialize();
	}

	private void initialize()
		throws QueryEvaluationException
	{
		Collection<BindingSet> bindingSets;
		Collection<Entry> entries;

		if (_ordered) {
			bindingSets = new ArrayList<BindingSet>();
			entries = buildOrderedEntries();
		}
		else {
			bindingSets = new HashSet<BindingSet>();
			entries = buildUnorderedEntries();
		}

		for (Entry entry : entries) {
			QueryBindingSet sol = new QueryBindingSet();

			for (String name : _group.getGroupBindingNames()) {
				Value value = entry.getPrototype().getValue(name);
				if (value != null) {
					sol.addBinding(name, value);
				}
			}

			for (AggregateBinding b : _group.getAggregateBindings()) {
				Value value = processAggregate(entry.getSolutions(), b.getOperator());
				if (value != null) {
					sol.addBinding(b.getName(), value);
				}
			}

			bindingSets.add(sol);
		}

		_iterator = bindingSets.iterator();
	}

	private Collection<Entry> buildOrderedEntries()
		throws QueryEvaluationException
	{
		Map<Key, Entry> entries = new HashMap<Key, Entry>();
		List<Entry> orderedEntries = new ArrayList<Entry>();

		CloseableIteration<BindingSet, QueryEvaluationException> iter = _strategy.evaluate(_group.getArg(),
				_bindings);
		while (iter.hasNext()) {
			BindingSet sol = iter.next();
			Key key = new Key(sol);
			Entry entry = entries.get(key);

			if (entry == null) {
				entry = new Entry(sol);
				entries.put(key, entry);
				orderedEntries.add(entry);
			}

			entry.addSolution(sol);
		}
		iter.close();

		return orderedEntries;
	}

	private Collection<Entry> buildUnorderedEntries()
		throws QueryEvaluationException
	{
		Map<Key, Entry> entries = new HashMap<Key, Entry>();

		CloseableIteration<BindingSet, QueryEvaluationException> iter = _strategy.evaluate(_group.getArg(),
				_bindings);
		while (iter.hasNext()) {
			BindingSet sol = iter.next();
			Key key = new Key(sol);
			Entry entry = entries.get(key);

			if (entry == null) {
				entry = new Entry(sol);
				entries.put(key, entry);
			}

			entry.addSolution(sol);
		}
		iter.close();

		return entries.values();
	}

	private Value processAggregate(Set<BindingSet> bindingSets, AggregateOperator operator) throws QueryEvaluationException {
		if (operator instanceof Count) {
			Count countOp = (Count)operator;

			ValueExpr arg = countOp.getArg();

			if (arg != null) {
				Set<Value> values = makeValueSet(arg, bindingSets);
				return new LiteralImpl(Integer.toString(values.size()), XMLSchema.INTEGER);
			}
			else {
				return new LiteralImpl(Integer.toString(bindingSets.size()), XMLSchema.INTEGER);
			}
		}
		else if (operator instanceof Min) {
			Min minOp = (Min)operator;

			Set<Value> values = makeValueSet(minOp.getArg(), bindingSets);

			// FIXME: handle case where 'values' is empty
			double min = Double.POSITIVE_INFINITY;
			for (Value v : values) {
				if (v instanceof Literal) {
					Literal l = (Literal)v;
					try {
						min = Math.min(min, Double.parseDouble(l.getLabel()));
					}
					catch (NumberFormatException e) {
						// ignore
					}
				}
			}

			return new LiteralImpl(Double.toString(min), XMLSchema.DOUBLE);
		}
		else if (operator instanceof Max) {
			Max maxOp = (Max)operator;

			Set<Value> values = makeValueSet(maxOp.getArg(), bindingSets);

			// FIXME: handle case where 'values' is empty
			double max = Double.NEGATIVE_INFINITY;
			for (Value v : values) {
				if (v instanceof Literal) {
					Literal l = (Literal)v;
					try {
						max = Math.max(max, Double.parseDouble(l.getLabel()));
					}
					catch (NumberFormatException e) {
						// ignore
					}
				}
			}

			return new LiteralImpl(Double.toString(max), XMLSchema.DOUBLE);
		}

		return null;
	}

	private Set<Value> makeValueSet(ValueExpr arg, Set<BindingSet> bindingSets) throws QueryEvaluationException {
		Set<Value> valueSet = new HashSet<Value>();

		for (BindingSet s : bindingSets) {
			Value value = _strategy.getValue(arg, s);
			if (value != null) {
				valueSet.add(value);
			}
		}

		return valueSet;
	}

	public boolean hasNext() {
		return _iterator.hasNext();
	}

	public BindingSet next() {
		return _iterator.next();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws QueryEvaluationException
	{
		_strategy = null;
		_group = null;
		super.close();
	}

	/**
	 * A unique key for a set of existing bindings.
	 * 
	 * @author David Huynh
	 */
	protected class Key {

		BindingSet _bindingSet;

		int _hash;

		public Key(BindingSet bindingSet) {
			_bindingSet = bindingSet;

			for (String name : _group.getGroupBindingNames()) {
				Value value = _bindingSet.getValue(name);
				if (value != null) {
					_hash ^= value.hashCode();
				}
			}
		}

		@Override
		public int hashCode()
		{
			return _hash;
		}

		@Override
		public boolean equals(Object other)
		{
			if (other instanceof Key && other.hashCode() == _hash) {
				BindingSet otherSolution = ((Key)other)._bindingSet;

				for (String name : _group.getGroupBindingNames()) {
					Value v1 = _bindingSet.getValue(name);
					Value v2 = otherSolution.getValue(name);

					if (!ObjectUtil.nullEquals(v1, v2)) {
						return false;
					}
				}

				return true;
			}

			return false;
		}
	}

	protected class Entry {

		private BindingSet _prototype;

		private Set<BindingSet> _bindingSets;

		public Entry(BindingSet prototype) {
			_prototype = prototype;
			_bindingSets = new HashSet<BindingSet>();
		}

		public BindingSet getPrototype() {
			return _prototype;
		}

		public void addSolution(BindingSet bindingSet) {
			_bindingSets.add(bindingSet);
		}

		public Set<BindingSet> getSolutions() {
			return _bindingSets;
		}
	}
}
