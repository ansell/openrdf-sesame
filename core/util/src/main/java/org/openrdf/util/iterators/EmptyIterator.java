/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */

package org.openrdf.util.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator that does not contain any elements.
 */
public class EmptyIterator<E> implements Iterator<E> {

	public boolean hasNext() {
		return false;
	}

	public E next() {
		throw new NoSuchElementException();
	}

	public void remove() {
		throw new IllegalStateException("Empty iterator does not contain any elements");
	}
}
