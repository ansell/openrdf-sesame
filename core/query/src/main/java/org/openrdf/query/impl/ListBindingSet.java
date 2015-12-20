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
package org.openrdf.query.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Value;
import org.openrdf.query.AbstractBindingSet;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;

/**
 * A List-based implementation of the {@link BindingSet} interface.
 * 
 * @author Arjohn Kampman
 */
public class ListBindingSet extends AbstractBindingSet {

	private static final long serialVersionUID = -2907809218835403743L;

	private final List<String> bindingNames;

	private final List<? extends Value> values;

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

		this.bindingNames = bindingNames;
		this.values = values;
	}

	public Set<String> getBindingNames() {
		return new LinkedHashSet<String>(bindingNames);
	}

	public Value getValue(String bindingName) {
		int idx = bindingNames.indexOf(bindingName);

		if (idx != -1) {
			return values.get(idx);
		}

		return null;
	}

	public Binding getBinding(String bindingName) {
		Value value = getValue(bindingName);

		if (value != null) {
			return new SimpleBinding(bindingName, value);
		}

		return null;
	}

	public boolean hasBinding(String bindingName) {
		return getValue(bindingName) != null;
	}

	public Iterator<Binding> iterator() {
		return new ListBindingSetIterator();
	}

	public int size() {
		int size = 0;

		for (Value value : values) {
			if (value != null) {
				size++;
			}
		}

		return size;
	}

	/*------------------------------------*
	 * Inner class ListBindingSetIterator *
	 *------------------------------------*/

	private class ListBindingSetIterator implements Iterator<Binding> {

		private int index = -1;

		public ListBindingSetIterator() {
			findNextElement();
		}

		private void findNextElement() {
			for (index++; index < values.size(); index++) {
				if (values.get(index) != null) {
					break;
				}
			}
		}

		public boolean hasNext() {
			return index < values.size();
		}

		public Binding next() {
			Binding result = new SimpleBinding(bindingNames.get(index), values.get(index));
			findNextElement();
			return result;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
