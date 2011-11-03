/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2011.
 *
 * Licensed under the Aduna BSD-style license.
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

	/**
	 * Throws an {@link UnsupportedOperationException}.
	 */
	public void remove() {
		throw new UnsupportedOperationException();
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
