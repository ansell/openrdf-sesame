/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.iterator;

import java.util.Iterator;

/**
 * An iterator that delays the creation of the underlying iterator until it is
 * being accessed. This is mainly useful for situations where iterator creation
 * adds considerable overhead but where the iterator may not actually be used,
 * or where a created iterator consumes scarce resources like JDBC-connections
 * or memory. Subclasses must implement the <tt>createIterator</tt> method,
 * which is called once when the iterator is first needed.
 */
public abstract class DelayedIterator<E> extends CloseableIteratorBase<E> {

	/*-----------*
	 * Variables *
	 *-----------*/
	
	private Iterator<? extends E> _iter;
	
	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new DelayedIterator.
	 */
	public DelayedIterator() {
		super();
	}
	
	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Creates the iterator that should be iterated over. This method is called
	 * only once, when the iterator is first needed.
	 */
	protected abstract Iterator<? extends E> createIterator();
	
	private void _initialize() {
		if (_iter == null && !isClosed()) {
			_iter = createIterator();
		}
	}

	/**
	 * Calls the <tt>hasNext</tt> method of the underlying iterator.
	 */
	public boolean hasNext() {
		_initialize();
		
		return _iter.hasNext();
	}

	/**
	 * Calls the <tt>next</tt> method of the underlying iterator.
	 */
	public E next() {
		_initialize();
		
		return _iter.next();
	}

	/**
	 * Calls the <tt>remove</tt> method of the underlying iterator.
	 */
	public void remove() {
		_initialize();
		
		_iter.remove();
	}

	/**
	 * Closed this iterator and also closes the underlying iterator if it is a
	 * {@link CloseableIterator}.
	 */
	public void close() {
		if (_iter != null && !isClosed()) {
			Iterators.closeCloseable(_iter);
		}

		super.close();
	}
}
