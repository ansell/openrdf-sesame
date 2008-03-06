/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.iterator;

import java.util.Iterator;

/**
 * A CloseableIterator that converts an iterator over objects of type <tt>S</tt>
 * (the source type) to an iterator over objects of type <tt>T</tt> (the target
 * type).
 */
public abstract class ConvertingIterator<S, T> extends CloseableIteratorBase<T> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The source type iterator.
	 */
	private Iterator<? extends S> _iter;
	
	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new ConvertingIterator that operates on the supplied source
	 * type iterator.
	 *
	 * @param iter The source type iterator for this
	 * <tt>ConvertingIterator</tt>, must not be <tt>null</tt>.
	 */
	public ConvertingIterator(Iterator<? extends S> iter) {
		assert iter != null;
		_iter = iter;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Converts a source type object to a target type object.
	 */
	protected abstract T convert(S sourceObject);
	
	/**
	 * Checks whether the source type iterator contains more elements, closing
	 * this iterator when this is not the case.
	 * 
	 * @return <tt>true</tt> if this iterator hasn't been closed yet and the
	 * source type iterator contains more elements, <tt>false</tt> otherwise.
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
	public T next() {
		if (isClosed()) {
			throw new IllegalStateException("Iterator has been closed");
		}
		
		return convert(_iter.next());
	}
	
	/**
	 * Unsupported operation, throws an <tt>UnsupportedOperationException</tt>.
	 * 
	 * @throws UnsupportedOperationException Is always thrown.
	 */
	public void remove() {
		throw new UnsupportedOperationException();
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
