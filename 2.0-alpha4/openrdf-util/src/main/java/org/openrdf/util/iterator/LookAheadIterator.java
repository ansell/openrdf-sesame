/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.iterator;

import java.util.NoSuchElementException;

/**
 * An iterator that looks one element ahead, if necessary, to handle calls to
 * {@link #hasNext}. This is a convenient super class for iterators that have no
 * easy way to tell if there are any more results, but still should implement
 * the <tt>java.util.Iterator</tt> interface.
 */
public abstract class LookAheadIterator<E> extends CloseableIteratorBase<E> {
	
	/*-----------*
	 * Variables *
	 *-----------*/

	private E _nextElement;
	
	/*--------------*
	 * Constructors *
	 *--------------*/

	public LookAheadIterator() {
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the next element. Subclasses should implement this method so that it
	 * returns the next element.
	 * 
	 * @return The next element, or <tt>null</tt> if no more elements are
	 * available.
	 */
	protected abstract E getNextElement();
	
	// implements Iterator.hasNext()
	public final boolean hasNext() {
		_lookAhead();
		
		return _nextElement != null;
	}

	// implements Iterator.next()
	public final E next() {
		_lookAhead();

		if (_nextElement != null) {
			E result = _nextElement;
			_nextElement = null;
			return result;
		}
		else {
			throw new NoSuchElementException();
		}
	}
	
	/**
	 * Fetches the next element if it hasn't been fetched yet and stores it in
	 * <tt>_nextElement</tt>.
	 */
	private void _lookAhead() {
		if (_nextElement == null && !isClosed()) {
			_nextElement = getNextElement();
			
			if (_nextElement == null) {
				close();
			}
		}		
	}

	/**
	 * Throws an UnsupportedOperationException.
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	// overrides CloseableIteratorBase.close()
	public void close() {
		_nextElement = null;
		super.close();
	}
}
