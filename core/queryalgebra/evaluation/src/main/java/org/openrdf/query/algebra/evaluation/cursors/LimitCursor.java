/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import org.openrdf.results.Cursor;
import org.openrdf.results.base.CursorWrapper;
import org.openrdf.store.StoreException;

/**
 * An Iteration that limits the amount of elements that it returns from an
 * underlying Iteration to a fixed amount. This class returns the first
 * <tt>limit</tt> elements from the underlying Iteration and drops the rest.
 * 
 * @author James Leigh
 */
public class LimitCursor<E> extends CursorWrapper<E> {

	private int limit;

	public LimitCursor(Cursor<? extends E> delegate, int limit) {
		super(delegate);
		this.limit = limit;
	}

	public E next()
		throws StoreException
	{
		if (--limit < 0)
			return null;
		return super.next();
	}
}
