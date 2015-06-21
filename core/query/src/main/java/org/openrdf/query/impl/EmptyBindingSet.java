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

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;

/**
 * An immutable empty BindingSet.
 * 
 * @author Arjohn Kampman
 */
public class EmptyBindingSet implements BindingSet {

	private static final long serialVersionUID = -6010968140688315954L;

	private static final EmptyBindingSet singleton = new EmptyBindingSet();

	public static BindingSet getInstance() {
		return singleton;
	}

	private EmptyBindingIterator iter = new EmptyBindingIterator();

	public Iterator<Binding> iterator() {
		return iter;
	}

	public Set<String> getBindingNames() {
		return Collections.emptySet();
	}

	public Binding getBinding(String bindingName) {
		return null;
	}

	public boolean hasBinding(String bindingName) {
		return false;
	}

	public Value getValue(String bindingName) {
		return null;
	}

	public int size() {
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof BindingSet) {
			return ((BindingSet)o).size() == 0;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public String toString() {
		return "[]";
	}

	/*----------------------------------*
	 * Inner class EmptyBindingIterator *
	 *----------------------------------*/

	private static class EmptyBindingIterator implements Iterator<Binding> {

		public boolean hasNext() {
			return false;
		}

		public Binding next() {
			throw new NoSuchElementException();
		}

		public void remove() {
			throw new IllegalStateException();
		}
	}
}
