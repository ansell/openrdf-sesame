/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import info.aduna.concurrent.locks.Lock;

import org.openrdf.StoreException;
import org.openrdf.query.Cursor;
import org.openrdf.query.base.CursorWrapper;

/**
 * An Iteration that holds on to a lock until the Iteration is closed. Upon
 * closing, the underlying Iteration is closed before the lock is released. This
 * iterator closes itself as soon as all elements have been read.
 * 
 * @author James Leigh
 */
public class LockingCursor<E> extends CursorWrapper<E> {

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
