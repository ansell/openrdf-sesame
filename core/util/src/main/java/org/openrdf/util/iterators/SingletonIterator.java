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

package org.openrdf.util.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An iterator that contains exactly one element.
 */
public class SingletonIterator<E> implements Iterator<E> {

	private final AtomicReference<E> value;

	/**
	 * Creates a new EmptyIterator.
	 */
	public SingletonIterator(E value) {
		this.value = new AtomicReference<E>(value);
	}

	public boolean hasNext() {
		return value.get() != null;
	}

	public E next() {
		E result = value.getAndSet(null);
		if (result == null) {
			throw new NoSuchElementException();
		}
		return result;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
