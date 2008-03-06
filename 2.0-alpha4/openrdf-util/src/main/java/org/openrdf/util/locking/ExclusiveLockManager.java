/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.locking;

/**
 * A lock manager for exclusive locks.
 */
public class ExclusiveLockManager {

	/**
	 * Flag indicating whether the single exclusive lock is currently in use.
	 */
	private boolean _lockInUse;

	/**
	 * Creates an ExclusiveLockManager.
	 */
	public ExclusiveLockManager() {
		_lockInUse = false;
	}

	/**
	 * Gets the exclusive lock. This method blocks when the exclusive lock is
	 * currently in use until it is released.
	 */
	public synchronized Lock getExclusiveLock()
		throws InterruptedException
	{
		while (_lockInUse) {
			// Someone else currently has the lock
			wait();
		}

		_lockInUse = true;

		return new AbstractLock() {
			protected void _releaseLock() {
				_releaseExclusiveLock();
			}
		};
	}

	// Method called by inner class ExclusiveLock
	private synchronized void _releaseExclusiveLock() {
		_lockInUse = false;
		notifyAll();
	}
}
