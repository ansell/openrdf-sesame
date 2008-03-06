/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.iterator;

import java.util.NoSuchElementException;

/**
 * An iterator that contains exactly one element.
 */
public class SingletonIterator<E> extends CloseableIteratorBase<E> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private E _value;
	
	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new EmptyIterator.
	 */
	public SingletonIterator(E value) {
		_value = value;
	}

	/*---------*
	 * Methods *
	 *---------*/

	// implements Iterator.hasNext()
	public boolean hasNext() {
		return _value != null;
	}

	// implements Iterator.next()
	public E next() {
		if (_value != null) {
			E result = _value;
			_value = null;
			return result;
		}
		else {
			throw new NoSuchElementException();
		}
	}
	
	// implements Iterator.remove()
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() {
		_value = null;
		super.close();
	}
}
