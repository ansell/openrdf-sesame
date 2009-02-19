/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.cursor;

import org.openrdf.store.StoreException;

/**
 * A Cursor that wraps another Cursor, applying a filter on the objects that are
 * returned. Subclasses must implement the {@link #accept(Object)} method to
 * indicate which objects should be returned.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public abstract class FilteringCursor<E> extends DelegatingCursor<E> {

	public FilteringCursor(Cursor<? extends E> delegate) {
		super(delegate);
	}

	/**
	 * Tests whether or not the specified object should be returned by this
	 * cursor. All objects from the wrapped cursor pass through this method in
	 * the same order as they are coming from the wrapped cursor.
	 * 
	 * @param object
	 *        The object to be tested.
	 * @return <tt>true</tt> if the object should be returned, <tt>false</tt>
	 *         otherwise.
	 */
	protected abstract boolean accept(E object)
		throws StoreException;

	@Override
	public final E next()
		throws StoreException
	{
		E next;
		while ((next = super.next()) != null) {
			if (accept(next)) {
				return next;
			}
		}

		return null;
	}
}
