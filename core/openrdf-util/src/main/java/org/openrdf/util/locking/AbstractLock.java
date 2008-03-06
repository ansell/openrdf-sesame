/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.locking;

import org.openrdf.util.log.ThreadLog;

/**
 * An abstract implementation of the Lock interface. Subclasses only need to
 * implement the protected <tt>_releaseLock</tt> method.
 *
 */
public abstract class AbstractLock implements Lock {

	/**
	 * Flag indicating whether this lock is active.
	 */
	private boolean _isActive = true;

	// Implements Lock.isActive()
	public synchronized boolean isActive() {
		return _isActive;
	}

	// Implements Lock.release()
	public synchronized void release() {
		if (_isActive) {
			try {
				_releaseLock();
			}
			finally {
				_isActive = false;
			}
		}
	}

	/**
	 * This method is called when someone calls the <tt>release()</tt> method
	 * on an active lock. This method is called only once on an object as the
	 * <tt>release()</tt> method flags the lock as inactive after the first call
	 * to it.
	 */
	protected abstract void _releaseLock();

	// Overrides Object.finalize()
	protected void finalize()
		throws Throwable
	{
		if (_isActive) {
			ThreadLog.warning("Releasing active lock due to object destruction");

			try {
				_releaseLock();
			}
			finally {
				_isActive = false;
			}
		}

		super.finalize();
	}
}
