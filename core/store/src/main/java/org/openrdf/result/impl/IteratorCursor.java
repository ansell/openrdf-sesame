/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.result.impl;

import java.util.Iterator;

import org.openrdf.result.Cursor;

/**
 * An Cursor that can convert an {@link Iterator} to a {@link Cursor}.
 * 
 * @author James Leigh
 */
public class IteratorCursor<E> implements Cursor<E> {

	private final Iterator<? extends E> iter;

	public IteratorCursor(Iterator<? extends E> iter) {
		assert iter != null : "iterator must not be null";
		this.iter = iter;
	}

	public E next() {
		if (iter.hasNext()) {
			return iter.next();
		}
		return null;
	}

	public void close() {
		// no-op
	}

	@Override
	public String toString() {
		return "Iterator " + iter.toString();
	}
}
