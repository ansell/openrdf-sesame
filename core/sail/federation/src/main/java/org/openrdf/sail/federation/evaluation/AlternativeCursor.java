/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.evaluation;

import org.openrdf.cursor.Cursor;
import org.openrdf.store.StoreException;

/**
 * If the primary cursor is empty, use the alternative cursor.
 * 
 * @author James Leigh
 */
public class AlternativeCursor<E> implements Cursor<E> {

	private Cursor<? extends E> delegate;

	private Cursor<? extends E> primary;

	private Cursor<? extends E> alternative;

	public AlternativeCursor(Cursor<? extends E> primary, Cursor<? extends E> alternative) {
		this.alternative = alternative;
		this.primary = primary;
	}

	public void close()
		throws StoreException
	{
		primary.close();
		alternative.close();
	}

	public E next()
		throws StoreException
	{
		if (delegate == null) {
			E next = primary.next();
			if (next == null) {
				delegate = alternative;
			}
			else {
				delegate = primary;
				return next;
			}
		}
		return delegate.next();
	}

	@Override
	public String toString() {
		String name = getClass().getName().replaceAll("^.*\\.|Cursor$", "");
		return name + "\n\t" + primary.toString() + "\n\t" + alternative.toString();
	}
}
