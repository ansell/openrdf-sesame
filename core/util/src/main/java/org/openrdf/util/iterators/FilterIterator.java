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
