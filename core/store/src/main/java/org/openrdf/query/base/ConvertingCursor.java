/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.base;

import org.openrdf.query.Cursor;
import org.openrdf.store.StoreException;

/**
 * A Cursor that converts another cursor over objects of type <tt>S</tt> (the
 * source type) to an cursor over objects of type <tt>T</tt> (the target type).
 * 
 * @author James Leigh
 */
public abstract class ConvertingCursor<S, T> implements Cursor<T> {

	private Cursor<? extends S> cursor;

	public ConvertingCursor(Cursor<? extends S> cursor) {
		this.cursor = cursor;
	}

	public T next()
		throws StoreException
	{
		S next = cursor.next();
		if (next != null)
			return convert(next);
		return null;
	}

	public void close()
		throws StoreException
	{
		cursor.close();
	}

	public String toString() {
		return getName() + " " + cursor.toString();
	}

	protected String getName() {
		return getClass().getName().replaceAll("^.*\\.|Cursor$", "");
	}

	protected abstract T convert(S next)
		throws StoreException;

}
