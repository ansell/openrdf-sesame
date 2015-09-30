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
package org.eclipse.rdf4j.query.impl;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;

/**
 * A Map-based implementation of the {@link BindingSet} interface.
 */
public class MapBindingSet implements BindingSet {

	private static final long serialVersionUID = -8857324525220429607L;

	private final Map<String, Binding> bindings;

	public MapBindingSet() {
		this(8);
	}

	/**
	 * Creates a new Map-based BindingSet with the specified initial capacity.
	 * Bindings can be added to this binding set using the {@link #addBinding}
	 * methods.
	 * 
	 * @param capacity
	 *        The initial capacity of the created BindingSet object.
	 */
	public MapBindingSet(int capacity) {
		// Create bindings map, compensating for HashMap's load factor
		bindings = new LinkedHashMap<String, Binding>(capacity * 2);
	}

	/**
	 * Adds a binding to the binding set.
	 * 
	 * @param name
	 *        The binding's name.
	 * @param value
	 *        The binding's value.
	 */
	public void addBinding(String name, Value value) {
		addBinding(new SimpleBinding(name, value));
	}

	/**
	 * Adds a binding to the binding set.
	 * 
	 * @param binding
	 *        The binding to add to the binding set.
	 */
	public void addBinding(Binding binding) {
		bindings.put(binding.getName(), binding);
	}

	/**
	 * Removes a binding from the binding set.
	 * 
	 * @param name
	 *        The binding's name.
	 */
	public void removeBinding(String name) {
		bindings.remove(name);
	}

	/**
	 * Removes all bindings from the binding set.
	 */
	public void clear() {
		bindings.clear();
	}

	public Iterator<Binding> iterator() {
		return bindings.values().iterator();
	}

	public Set<String> getBindingNames() {
		return bindings.keySet();
	}

	public Binding getBinding(String bindingName) {
		return bindings.get(bindingName);
	}

	public boolean hasBinding(String bindingName) {
		return bindings.containsKey(bindingName);
	}

	public Value getValue(String bindingName) {
		Binding binding = getBinding(bindingName);

		if (binding != null) {
			return binding.getValue();
		}

		return null;
	}

	public int size() {
		return bindings.size();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		else if (other instanceof BindingSet) {
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

			// All bindings have been matched, sets are equal if this binding set
			// doesn't have any additional bindings.
			return otherSize == bindings.size();
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = 0;

		for (Binding binding : this) {
			hashCode ^= binding.hashCode();
		}

		return hashCode;
	}

	@Override
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
}
