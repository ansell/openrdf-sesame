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
