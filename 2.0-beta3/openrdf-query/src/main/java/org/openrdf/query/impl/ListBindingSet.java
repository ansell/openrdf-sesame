/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;

import org.openrdf.model.Value;

/**
 * A List-based implementation of the {@link BindingSet} interface.
 */
public class ListBindingSet implements BindingSet {

	/*-----------*
	 * Variables *
	 *-----------*/

	private List<String> _bindingNames;

	private List<? extends Value> _values;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new List-based BindingSet containing the supplied bindings.
	 * <em>The supplied list of binding names is assumed to be constant</em>;
	 * care should be taken that the contents of this list doesn't change after
	 * supplying it to this solution. The number of supplied values must be equal
	 * to the number of the binding names.
	 * 
	 * @param names
	 *        The binding names.
	 * @param values
	 *        The binding values.
	 */
	public ListBindingSet(List<String> names, Value... values) {
		this(names, Arrays.asList(values));
	}

	/**
	 * Creates a new List-based BindingSet containing the supplied bindings.
	 * <em>The supplied lists are assumed to be constant</em>; care should be
	 * taken that the contents of these lists don't change after supplying them
	 * to this solution. The number of supplied values must be equal to the
	 * number of the binding names.
	 * 
	 * @param bindingNames
	 *        The binding names.
	 * @param values
	 *        The binding values.
	 */
	public ListBindingSet(List<String> bindingNames, List<? extends Value> values) {
		assert bindingNames.size() == values.size() : "number of binding names and values not equal";

		_bindingNames = bindingNames;
		_values = values;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Value getValue(String bindingName) {
		int idx = _bindingNames.indexOf(bindingName);

		if (idx != -1) {
			return _values.get(idx);
		}

		return null;
	}

	public Binding getBinding(String bindingName) {
		Value value = getValue(bindingName);

		if (value != null) {
			return new BindingImpl(bindingName, value);
		}

		return null;
	}

	public boolean hasBinding(String bindingName) {
		return _bindingNames.contains(bindingName);
	}

	public Iterator<Binding> iterator() {
		return new ListBindingSetIterator();
	}

	public int size() {
		int size = 0;

		for (Value value : _values) {
			if (value != null) {
				size++;
			}
		}

		return size;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}

		if (other instanceof BindingSet) {
			int otherSize = 0;

			// Compare other's bindings to own
			for (Binding binding : (BindingSet)other) {
				Value ownValue = getValue(binding.getName());

				if (!binding.getValue().equals(ownValue)) {
					// Unequal bindings for this name
					return false;
				}

				otherSize++;
			}

			// All bindings have been matched, sets are equal if this solution
			// doesn't have any additional bindings.
			int thisSize = 0;
			for (Value value : _values) {
				if (value != null) {
					thisSize++;
				}
			}

			return thisSize == otherSize;
		}

		return false;
	}

	public int hashCode() {
		int hashCode = 0;

		for (Binding binding : this) {
			hashCode ^= binding.hashCode();
		}

		return hashCode;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(32 * size());

		sb.append('[');

		Iterator<Binding> iter = iterator();
		while (iter.hasNext()) {
			sb.append(iter.next().toString());
			if (iter.hasNext()) {
				sb.append(';');
			}
		}

		sb.append(']');

		return sb.toString();
	}

	/*------------------------------------*
	 * Inner class ListBindingSetIterator *
	 *------------------------------------*/

	private class ListBindingSetIterator implements Iterator<Binding> {

		private int _index = -1;

		public ListBindingSetIterator() {
			_findNextElement();
		}

		private void _findNextElement() {
			for (_index++; _index < _values.size(); _index++) {
				if (_values.get(_index) != null) {
					break;
				}
			}
		}

		public boolean hasNext() {
			return _index < _values.size();
		}

		public Binding next() {
			Binding result = new BindingImpl(_bindingNames.get(_index), _values.get(_index));
			_findNextElement();
			return result;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
