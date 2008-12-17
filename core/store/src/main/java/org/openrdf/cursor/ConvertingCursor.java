/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.cursor;

import org.openrdf.store.StoreException;

/**
 * A Cursor that converts another cursor over objects of type <tt>S</tt> (the
 * source type) to a cursor over objects of type <tt>T</tt> (the target type).
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public abstract class ConvertingCursor<S, T> implements Cursor<T> {

	private final Cursor<? extends S> cursor;

	public ConvertingCursor(Cursor<? extends S> cursor) {
		assert cursor != null : "cursor must not be null";
		this.cursor = cursor;
	}

	/**
	 * Converts a source type object to a target type object.
	 */
	protected abstract T convert(S sourceObject)
		throws StoreException;

	public T next()
		throws StoreException
	{
		S next = cursor.next();
		if (next != null) {
			return convert(next);
		}
		return null;
	}

	public void close()
		throws StoreException
	{
		cursor.close();
	}

	@Override
	public String toString() {
		return getName() + " " + cursor.toString();
	}

	protected String getName() {
		return getClass().getName().replaceAll("^.*\\.|Cursor$", "");
	}
}
