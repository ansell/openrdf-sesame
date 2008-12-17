/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.result.base;

import org.openrdf.result.Cursor;
import org.openrdf.store.StoreException;

/**
 * Abstract superclass for Cursors that wrap other Cursors. This abstract class
 * <tt>CursorWrapper</tt> itself provides default methods that forward method
 * calls to the wrapped Cursor. Subclasses of <tt>CursorWrapper</tt> should
 * override some of these methods and may also provide additional methods and
 * fields.
 * 
 * @author James Leigh
 */
public abstract class CursorWrapper<E> implements Cursor<E> {

	private Cursor<? extends E> delegate;

	public CursorWrapper(Cursor<? extends E> delegate) {
		this.delegate = delegate;
	}

	public void close()
		throws StoreException
	{
		delegate.close();
	}

	public E next()
		throws StoreException
	{
		return delegate.next();
	}

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
