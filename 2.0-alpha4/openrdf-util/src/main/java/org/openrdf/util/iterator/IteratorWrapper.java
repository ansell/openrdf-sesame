/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.iterator;

import java.util.Iterator;

/**
 * Abstract superclass for iterators that wrap another iterator. The abstract
 * class <tt>IteratorWrapper</tt> itself provides default methods that forward
 * method calls to the wrapped iterator, closing itself when exhausted.
 * Subclasses of <tt>IteratorWrapper</tt> should override some of these methods
 * and may also provide additional methods and fields.
 */
public class IteratorWrapper<E> extends CloseableIteratorBase<E> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The wrapped iterator.
	 */
	private Iterator<? extends E> _iter;
	
	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new IteratorWrapper that operates on the supplied iterator.
	 *
	 * @param iter The wrapped iterator for this <tt>IteratorWrapper</tt>,
	 * must not be <tt>null</tt>.
	 */
	public IteratorWrapper(Iterator<? extends E> iter) {
		assert iter != null;
		_iter = iter;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Checks whether the wrapped iterator contains more elements, closing this
	 * iterator when this is not the case.
	 * 
	 * @return <tt>true</tt> if this iterator hasn't been closed yet and the
	 * wrapped iterator contains more elements, <tt>false</tt> otherwise.
	 */
	public boolean hasNext() {
		if (isClosed()) {
			return false;
		}

		if (_iter.hasNext()) {
			return true;
		}
		else {
			close();
			return false;
		}
	}

	/**
	 * Returns the next element from the wrapped iterator.
	 * 
	 * @throws java.util.NoSuchElementException If all elements have been
	 * returned.
	 * @throws IllegalStateException If the iterator has been closed.
	 */
	public E next() {
		if (isClosed()) {
			throw new IllegalStateException("Iterator has been closed");
		}
		
		return _iter.next();
	}
	
	/**
	 * Removes the last element that has been returned from the wrapped
	 * iterator.
	 * 
	 * @throws UnsupportedOperationException If the wrapped iterator does not
	 * support the <tt>remove</tt> operation.
	 * @throws IllegalStateException if the iterator has been closed, or if
	 * {@link #next} has not yet been called, or {@link #remove} has already
	 * been called after the last call to {@link #next}.
	 */
	public void remove() {
		if (isClosed()) {
			throw new IllegalStateException("Iterator has been closed");
		}

		_iter.remove();
	}

	/**
	 * Closed this iterator and also closes the wrapped iterator if it is a
	 * {@link CloseableIterator}.
	 */
	public void close() {
		if (!isClosed()) {
			Iterators.closeCloseable(_iter);
		}
		
		super.close();
	}
}
