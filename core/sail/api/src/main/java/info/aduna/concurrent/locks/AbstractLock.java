/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.concurrent.locks;

/**
 * An abstract implementation of the Lock interface. Subclasses only need to
 * implement the protected {@link #releaseLock()} method.
 * 
 * @author Arjohn Kampman
 */
abstract class AbstractLock implements Lock {

	/**
	 * Flag indicating whether this lock is active.
	 */
	private boolean isActive = true;

	/**
	 * Creates a new lock.
	 */
	public AbstractLock() {
	}

	public synchronized boolean isActive() {
		return isActive;
	}

	public synchronized void release() {
		if (isActive) {
			try {
				releaseLock();
			}
			finally {
				isActive = false;
			}
		}
	}

	/**
	 * This method is called when someone calls the <tt>release()</tt> method on
	 * an active lock. This method is called only once on an object as the
	 * <tt>release()</tt> method flags the lock as inactive after the first call
	 * to it.
	 */
	protected abstract void releaseLock();
}
