/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.concurrent.locks;


/**
 * A lock manager for exclusive locks.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class ExclusiveLockManager {

	/*
	 * ----------- Variables -----------
	 */

	private final LockManager lock;

	/*
	 * -------------- Constructors --------------
	 */

	/**
	 * Creates an ExclusiveLockManager.
	 */
	public ExclusiveLockManager() {
		this(false);
	}

	/**
	 * Creates an ExclusiveLockManager.
	 * 
	 * @param name
	 *        Common name used for debugging
	 * @param trackLocks
	 *        If create stack traces should be logged
	 */
	public ExclusiveLockManager(boolean trackLocks) {
		this.lock = new LockManager(trackLocks || Properties.lockTrackingEnabled());
	}

	/*
	 * --------- Methods ---------
	 */

	/**
	 * Gets the exclusive lock, if available. This method will return
	 * <tt>null</tt> if the exclusive lock is not immediately available.
	 */
	public Lock tryExclusiveLock() {
		if (lock.isActiveLock()) {
			return null;
		}
		synchronized (this) {
			if (lock.isActiveLock()) {
				return null;
			}
	
			return createLock();
		}
	}

	/**
	 * Gets the exclusive lock. This method blocks when the exclusive lock is
	 * currently in use until it is released.
	 */
	public synchronized Lock getExclusiveLock()
		throws InterruptedException
	{
		while (lock.isActiveLock()) {
			// Someone else currently has the lock
			lock.waitForActiveLocks();
		}

		return createLock();
	}

	private Lock createLock() {
		return lock.createLock("Exclusive");
	}
}
