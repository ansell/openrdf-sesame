/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.locking;


/**
 * A lock manager that manages a multi-read, single-write lock. This lock
 * manager allows multiple read locks to be active at the same time. The write
 * lock is exclusive, meaning that no other read- or write locks may be active
 * at the same time.
 */
public class MultiReadSingleWriteLockManager {

	/**
	 * Flag indicating whether a write lock has been requested.
	 */
	private boolean _writeRequested;

	/**
	 * Counter that keeps track of the numer of active read locks.
	 */
	private int _readingThreads;

	/**
	 * Creates a MultiReadSingleWriteLockManager.
	 */
	public MultiReadSingleWriteLockManager() {
		_writeRequested = false;
		_readingThreads = 0;
	}

	/**
	 * Gets a read lock. This method blocks when a write lock is in use or has
	 * been requested until the write lock is released.
	 */
	public synchronized Lock getReadLock()
		throws InterruptedException
	{
		// Wait for any writing threads to finish
		while (_writeRequested) {
			wait();
		}

		// No candidates for writing anymore, go ahead
		_readingThreads++;

		return new AbstractLock() {
			protected void _releaseLock() {
				_releaseReadLock();
			}
		};
	}

	/**
	 * Releases a read lock.
	 */
	private synchronized void _releaseReadLock() {
		_readingThreads--;

		if (_readingThreads == 0) {
			// Maybe someone wants to write?
			notifyAll();
		}
	}

	/**
	 * Gets an exclusive write lock. This method blocks when the write lock is
	 * in use or has already been requested until the write lock is released.
	 * This method also block when read locks are active until all of them are
	 * released.
	 */
	public synchronized Lock getWriteLock()
		throws InterruptedException
	{
		while (_writeRequested) {
			// Someone else wants to write
			wait();
		}

		_writeRequested = true;

		// Wait for the readingThreads to finish
		while (_readingThreads > 0) {
			wait();
		}

		return new AbstractLock() {
			protected void _releaseLock() {
				_releaseWriteLock();
			}
		};
	}

	/**
	 * Release a write lock.
	 */
	private synchronized void _releaseWriteLock() {
		_writeRequested = false;
		notifyAll();
	}
}
