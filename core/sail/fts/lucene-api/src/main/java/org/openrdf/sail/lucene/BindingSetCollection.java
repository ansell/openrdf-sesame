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
package org.openrdf.sail.lucene;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.openrdf.query.BindingSet;

class BindingSetCollection implements Collection<BindingSet> {

	private final Set<String> bindingNames;

	private final Collection<BindingSet> bindingSets;

	BindingSetCollection(Set<String> bindingNames, Collection<BindingSet> bindingSets) {
		this.bindingNames = bindingNames;
		this.bindingSets = bindingSets;
	}

	public Set<String> getBindingNames() {
		return bindingNames;
	}

	@Override
	public boolean add(BindingSet arg0) {
		return bindingSets.add(arg0);
	}

	@Override
	public boolean addAll(Collection<? extends BindingSet> c) {
		return bindingSets.addAll(c);
	}

	@Override
	public void clear() {
		bindingSets.clear();
	}

	@Override
	public boolean contains(Object o) {
		return bindingSets.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return bindingSets.containsAll(c);
	}

	@Override
	public boolean equals(Object o) {
		return bindingSets.equals(o);
	}

	@Override
	public int hashCode() {
		return bindingSets.hashCode();
	}

	@Override
	public boolean isEmpty() {
		return bindingSets.isEmpty();
	}

	@Override
	public Iterator<BindingSet> iterator() {
		return bindingSets.iterator();
	}

	@Override
	public boolean remove(Object o) {
		return bindingSets.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return bindingSets.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return bindingSets.retainAll(c);
	}

	@Override
	public int size() {
		return bindingSets.size();
	}

	@Override
	public Object[] toArray() {
		return bindingSets.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return bindingSets.toArray(a);
	}
}
