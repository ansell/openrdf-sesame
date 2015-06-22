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
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.Iterator;
import java.util.Set;

import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;

class BindingSetSize implements BindingSet {

	private static final long serialVersionUID = -7968068342865378845L;

	private final int size;

	public BindingSetSize(int size) {
		super();
		this.size = size;
	}

	public Binding getBinding(String bindingName) {
		throw new UnsupportedOperationException();
	}

	public Set<String> getBindingNames() {
		throw new UnsupportedOperationException();
	}

	public Value getValue(String bindingName) {
		throw new UnsupportedOperationException();
	}

	public boolean hasBinding(String bindingName) {
		throw new UnsupportedOperationException();
	}

	public Iterator<Binding> iterator() {
		throw new UnsupportedOperationException();
	}

	public int size() {
		return size;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + size;
	}
}