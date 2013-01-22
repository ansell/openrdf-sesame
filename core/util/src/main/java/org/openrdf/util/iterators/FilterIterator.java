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

/**
 * A CloseableIterator that wraps another iterator, applying a filter on the
 * objects that are returned. Subclasses must implement the <tt>accept</tt>
 * method to indicate which objects should be returned.
 */
public abstract class FilterIterator<E> implements Iterator<E> {

	private final Iterator<? extends E> filteredIter;

	private E nextElement;

	public FilterIterator(Iterator<? extends E> iter) {
		this.filteredIter = iter;
	}

	public boolean hasNext() {
		findNextElement();

		return nextElement != null;
	}

	public E next() {
		findNextElement();

		E result = nextElement;

		if (result != null) {
			nextElement = null;
			return result;
		}
		else {
			throw new NoSuchElementException();
		}
	}

	private void findNextElement() {
		while (nextElement == null && filteredIter.hasNext()) {
			E candidate = filteredIter.next();

			if (accept(candidate)) {
				nextElement = candidate;
			}
		}
	}

	public void remove() {
		filteredIter.remove();
	}

	/**
	 * Tests whether or not the specified object should be returned by this
	 * iterator. All objects from the wrapped iterator pass through this method
	 * in the same order as they are coming from the wrapped iterator.
	 * 
	 * @param object
	 *        The object to be tested.
	 * @return <tt>true</tt> if the object should be returned, <tt>false</tt>
	 *         otherwise.
	 * @throws X
	 */
	protected abstract boolean accept(E object);
}
