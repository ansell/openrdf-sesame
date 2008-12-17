/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.cursor;

import java.util.concurrent.atomic.AtomicBoolean;

import org.openrdf.store.StoreException;

/**
 * Abstract class offering common functionality for {@link Cursor}s that need to
 * check if they have been closed.
 * 
 * @author Arjohn Kampman
 */
public abstract class CheckedCursor<E> implements Cursor<E> {

	/**
	 * Flag indicating whether this cursor has been closed.
	 */
	private final AtomicBoolean closed = new AtomicBoolean(false);

	/**
	 * Checks whether this cursor has been closed.
	 * 
	 * @return <tt>true</tt> if the cursor has been closed, <tt>false</tt>
	 *         otherwise.
	 */
	public final boolean isClosed() {
		return closed.get();
	}

	/**
	 * Calls {@link #handleClose()} upon first call and makes sure this method
	 * gets called only once.
	 */
	public final void close()
		throws StoreException
	{
		if (closed.compareAndSet(false, true)) {
			handleClose();
		}
	}

	/**
	 * Called by {@link #close} when it is called for the first time. This method
	 * is only called once on each cursor. By default, this method does nothing.
	 */
	protected void handleClose()
		throws StoreException
	{
	}

	public E next()
		throws StoreException
	{
		if (isClosed()) {
			return null;
		}

		return checkedNext();
	}

	/**
	 * Returns the next element from this cursor. This method is called by
	 * {@link #next()} after checking whether the cursor has been closed.
	 * 
	 * @return the next element from this cursor, or <tt>null</tt> if the cursor
	 *         has no more elements.
	 */
	protected abstract E checkedNext()
		throws StoreException;
}
