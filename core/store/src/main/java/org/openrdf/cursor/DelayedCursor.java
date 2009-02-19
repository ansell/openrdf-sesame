/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.cursor;

import org.openrdf.store.StoreException;

/**
 * A cursor that delays the creation of the underlying cursor until it is being
 * accessed. This is mainly useful for situations where cursor creation adds
 * considerable overhead but where the cursor may not actually be used, or where
 * a created cursor consumes scarce resources like JDBC-connections or memory.
 * Subclasses must implement the {@link #createCursor()} method, which is called
 * once when the cursor is first needed.
 * 
 * @author Arjohn Kampman
 */
public abstract class DelayedCursor<E> extends CheckedCursor<E> {

	private Cursor<? extends E> delegate;

	/**
	 * Creates a new DelayedCursor.
	 */
	public DelayedCursor() {
		super();
	}

	/**
	 * Creates the cursor that should be iterated over. This method is called
	 * only once, when the cursor is first needed.
	 */
	protected abstract Cursor<? extends E> createCursor()
		throws StoreException;

	protected boolean cursorCreated() {
		synchronized (this) {
			return delegate != null;
		}
	}

	protected E checkedNext()
		throws StoreException
	{
		// Check without synchronization first to prevent overhead for the most
		// common case
		if (delegate == null && !isClosed()) {
			synchronized (this) {
				if (delegate == null && !isClosed()) {
					delegate = createCursor();
				}
			}
		}

		return delegate.next();
	}

	@Override
	protected void handleClose()
		throws StoreException
	{
		super.handleClose();

		synchronized (this) {
			if (delegate != null) {
				delegate.close();
			}
		}
	}

	@Override
	public String toString() {
		if (delegate != null) {
			return delegate.toString();
		}
		else {
			return super.toString();
		}
	}
}
