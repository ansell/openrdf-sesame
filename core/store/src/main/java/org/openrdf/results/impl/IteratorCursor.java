/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.results.impl;

import java.util.Iterator;

import org.openrdf.results.Cursor;

/**
 * An Cursor that can convert an {@link Iterator} to a {@link Cursor}.
 * 
 * @author James Leigh
 */
public class IteratorCursor<E> implements Cursor<E> {

	private Iterator<? extends E> iter;

	public IteratorCursor(Iterator<? extends E> iter) {
		this.iter = iter;
	}

	public E next() {
		if (iter.hasNext())
			return iter.next();
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
