/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.iterator;

import java.util.Iterator;

import org.openrdf.util.locking.Lock;

/**
 * An iterator that holds on to a lock until the iterator is closed. Upon
 * closing, the underlying iterator is closed before the lock is released.
 */
public class LockingIterator<E> extends IteratorWrapper<E> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The lock to release when the iterator is closed.
	 */
	private Lock _lock;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new LockingIterator.
	 *
	 * @param lock The lock to release when the itererator is closed, must not
	 * be <tt>null</tt>.
	 * @param iter The underlying iterator, must not be <tt>null</tt>.
	 */
	public LockingIterator(Lock lock, Iterator<? extends E> iter) {
		super(iter);
		
		assert lock != null;
		
		_lock = lock;
	}
	
	/*---------*
	 * Methods *
	 *---------*/

	// overrides IteratorWrapper.close()
	public void close() {
		if (!isClosed()) {
			try {
				super.close();
			}
			finally {
				_lock.release();
			}
		}
	}
}
