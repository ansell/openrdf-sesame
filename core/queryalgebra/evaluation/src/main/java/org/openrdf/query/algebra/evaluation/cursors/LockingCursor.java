/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import info.aduna.concurrent.locks.Lock;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.DelegatingCursor;
import org.openrdf.store.StoreException;

/**
 * An cursor that holds on to a lock until the cursor is closed. Upon closing,
 * the underlying cursor is closed before the lock is released. This cursor
 * closes itself as soon as all elements have been read.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class LockingCursor<E> extends DelegatingCursor<E> {

	private Lock lock;

	public LockingCursor(Lock lock, Cursor<? extends E> cursor) {
		super(cursor);
		this.lock = lock;
	}

	@Override
	public void close()
		throws StoreException
	{
		try {
			super.close();
		}
		finally {
			lock.release();
		}
	}
}
