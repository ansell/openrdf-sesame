/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
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
