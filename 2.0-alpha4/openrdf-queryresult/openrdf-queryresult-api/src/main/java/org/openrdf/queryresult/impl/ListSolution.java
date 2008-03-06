/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.queryresult.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.openrdf.queryresult.Binding;
import org.openrdf.queryresult.Solution;

import org.openrdf.model.Value;

/**
 * A List-based implementation of the {@link Solution} interface.
 */
public class ListSolution implements Solution {

	/*-----------*
	 * Variables *
	 *-----------*/

	private List<String> _bindingNames;

	private List<? extends Value> _values;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new List-based Solution containing the supplied bindings.
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
	public ListSolution(List<String> names, Value... values) {
		this(names, Arrays.asList(values));
	}

	/**
	 * Creates a new List-based Solution containing the supplied bindings.
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
	public ListSolution(List<String> bindingNames, List<? extends Value> values) {
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
		return new ListSolutionIterator();
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

		if (other instanceof Solution) {
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

	/*----------------------------------*
	 * Inner class ListSolutionIterator *
	 *----------------------------------*/

	private class ListSolutionIterator implements Iterator<Binding> {

		private int _index = -1;

		public ListSolutionIterator() {
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
