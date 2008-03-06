/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylogic;

import org.openrdf.model.Value;
import org.openrdf.queryresult.Binding;
import org.openrdf.queryresult.Solution;
import org.openrdf.queryresult.impl.BindingImpl;
import org.openrdf.queryresult.impl.MapSolution;
import org.openrdf.util.iterator.ConvertingIterator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An implementation of the {@link Solution} interface that is used to evalate
 * query object models. This implementations differs from {@link MapSolution} in
 * that it maps variable names to Value objects and that the Binding objects are
 * created lazily.
 */
public class QuerySolution implements Solution {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Map<String, Value> _bindings;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public QuerySolution() {
		this(8);
	}

	public QuerySolution(int capacity) {
		// Create bindings map with some extra space for new bindings and
		// compensating for HashMap's load factor
		_bindings = new HashMap<String, Value>(capacity * 2);
	}

	public QuerySolution(Solution solution) {
		this(solution.size());
		addAll(solution);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void addAll(Solution solution) {
		if (solution instanceof QuerySolution) {
			_bindings.putAll(((QuerySolution)solution)._bindings);
		}
		else {
			for (Binding binding : solution) {
				this.addBinding(binding.getName(), binding.getValue());
			}
		}
	}

	/**
	 * Adds a new binding to the solution. The binding's name must not already be
	 * part of this solution.
	 * 
	 * @param name
	 *        The binding's name, must not be bound in this solution already.
	 * @param value
	 *        The binding's value.
	 */
	public void addBinding(String name, Value value) {
		assert !_bindings.containsKey(name) : "variable is already bound";

		_bindings.put(name, value);
	}

	public Value getValue(String bindingName) {
		return _bindings.get(bindingName);
	}

	public Binding getBinding(String bindingName) {
		Value value = getValue(bindingName);
		
		if (value != null) {
			return new BindingImpl(bindingName, value);
		}
		
		return null;
	}

	public boolean hasBinding(String bindingName) {
		return _bindings.containsKey(bindingName);
	}

	public Iterator<Binding> iterator() {
		Iterator<Map.Entry<String, Value>> entries = _bindings.entrySet().iterator();

		return new ConvertingIterator<Map.Entry<String, Value>, Binding>(entries) {

			@Override
			protected Binding convert(Entry<String, Value> entry)
			{
				return new BindingImpl(entry.getKey(), entry.getValue());
			}
		};
	}

	public int size() {
		return _bindings.size();
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		else if (other instanceof QuerySolution) {
			return _bindings.equals(((QuerySolution)other)._bindings);
		}
		else if (other instanceof Solution) {
			int otherSize = 0;

			// Compare other's bindings to own
			for (Binding binding : (Solution)other) {
				Value ownValue = getValue(binding.getName());

				if (!binding.getValue().equals(ownValue)) {
					// Unequal bindings for this name
					return false;
				}

				otherSize++;
			}

			// All bindings have been matched, sets are equal if this solution
			// doesn't have any additional bindings.
			return otherSize == _bindings.size();
		}

		return false;
	}

	public int hashCode() {
		int hashCode = 0;

		for (Map.Entry<String, Value> entry : _bindings.entrySet()) {
			hashCode ^= entry.getKey().hashCode();
			Value value = entry.getValue();
			if (value != null)
				hashCode ^= value.hashCode();
		}

		return hashCode;
	}
}
