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

	private final Cursor<? extends S> delegate;

	public ConvertingCursor(Cursor<? extends S> delegate) {
		assert delegate != null : "delegate must not be null";
		this.delegate = delegate;
	}

	/**
	 * Converts a source type object to a target type object.
	 */
	protected abstract T convert(S sourceObject)
		throws StoreException;

	public T next()
		throws StoreException
	{
		S next = delegate.next();
		if (next != null) {
			return convert(next);
		}
		return null;
	}

	public void close()
		throws StoreException
	{
		delegate.close();
	}

	@Override
	public String toString() {
		return getName() + " " + delegate.toString();
	}

	protected String getName() {
		return getClass().getName().replaceAll("^.*\\.|Cursor$", "");
	}
}
