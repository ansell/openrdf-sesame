/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.cursor;

import java.util.Iterator;

/**
 * An cursor that iterates over an {@link Iterator}'s elements.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public class IteratorCursor<E> extends CheckedCursor<E> {

	private final Iterator<? extends E> iter;

	public IteratorCursor(Iterator<? extends E> iter) {
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
