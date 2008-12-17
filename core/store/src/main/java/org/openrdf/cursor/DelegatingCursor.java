/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.cursor;

import org.openrdf.store.StoreException;

/**
 * Abstract superclass for cursors that delegate calls to other cursors. This
 * class provides default method implementations that forward method calls to
 * the wrapped cursor. Subclasses of this class should override some of these
 * methods and may also provide additional methods and fields.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public abstract class DelegatingCursor<E> implements Cursor<E> {

	private final Cursor<? extends E> delegate;

	public DelegatingCursor(Cursor<? extends E> delegate) {
		this.delegate = delegate;
	}

	public E next()
		throws StoreException
	{
		return delegate.next();
	}

	public void close()
		throws StoreException
	{
		delegate.close();
	}

	@Override
	public String toString() {
		String name = getName().trim();
		if (name.contains("\n")) {
			return name.replace("\n", "\n\t") + "\n\t" + delegate.toString();
		}
		return name + " " + delegate.toString();
	}

	protected String getName() {
		return getClass().getName().replaceAll("^.*\\.|Cursor$", "");
	}
}
