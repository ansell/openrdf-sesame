/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.iterator;

import java.util.NoSuchElementException;

/**
 * An iterator that does not contain any elements.
 */
public class EmptyIterator<E> extends CloseableIteratorBase<E> {

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new EmptyIterator.
	 */
	public EmptyIterator() {
	}

	/*---------*
	 * Methods *
	 *---------*/

	// implements Iterator.hasNext()
	public boolean hasNext() {
		return false;
	}

	// implements Iterator.next()
	public E next() {
		throw new NoSuchElementException();
	}
	
	// implements Iterator.remove()
	public void remove() {
		throw new IllegalStateException("Empty iterator does not contain any elements");
	}
}
