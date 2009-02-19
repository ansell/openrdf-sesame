/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.cursor;

import java.util.Iterator;

/**
 * An cursor that iterates over a collection's elements.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public class CollectionCursor<E> extends CheckedCursor<E> {

	private final Iterator<? extends E> iter;

	public CollectionCursor(Iterable<? extends E> iterable) {
		this(iterable.iterator());
	}

	public CollectionCursor(Iterator<? extends E> iter) {
		assert iter != null : "iterator must not be null";
		this.iter = iter;
	}

	protected E checkedNext() {
		if (iter.hasNext()) {
			return iter.next();
		}
		return null;
	}

	@Override
	public String toString() {
		return "Iterator " + iter.toString();
	}
}
