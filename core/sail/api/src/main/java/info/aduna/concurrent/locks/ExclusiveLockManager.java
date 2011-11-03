/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.concurrent.locks;

import java.lang.ref.WeakReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A lock manager for exclusive locks.
 * 
 * @author Arjohn Kampman
 */
public class ExclusiveLockManager {

	/*
	 * ----------- Variables -----------
	 */

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Controls whether the lock manager will keep track of who owns the
	 * exclusive lock. Mainly useful for debugging.
	 */
	private final boolean trackLocks;

	/**
	 * Flag indicating whether the single exclusive lock is currently in use.
	 */
	private boolean lockInUse;

	/**
	 * A reference to the currently active lock, if any. Only used when lock
	 * tracking is enabled.
	 */
	@SuppressWarnings("unused")
	private WeakReference<Lock> activeLock;

	/*
	 * -------------- Constructors --------------
	 */

	/**
	 * Creates an ExclusiveLockManager.
	 */
	public ExclusiveLockManager() {
		this(false);
	}

	public ExclusiveLockManager(boolean trackLocks) {
		lockInUse = false;
		this.trackLocks = trackLocks || Properties.lockTrackingEnabled();
	}

	/*
	 * --------- Methods ---------
	 */

	/**
	 * Gets the exclusive lock, if available. This method will return
	 * <tt>null</tt> if the exclusive lock is not immediately available.
	 */
	public synchronized Lock tryExclusiveLock() {
		if (lockInUse) {
			return null;
		}

		return createLock();
	}

	/**
	 * Gets the exclusive lock. This method blocks when the exclusive lock is
	 * currently in use until it is released.
	 */
	public synchronized Lock getExclusiveLock()
		throws InterruptedException
	{
		while (lockInUse) {
			// Someone else currently has the lock
			wait();
		}

		return createLock();
	}

	private Lock createLock() {
		lockInUse = true;

		if (trackLocks) {
			Lock lock = new ExclusiveDebugLock(logger, true);

			// Keep track of who acquired the lock
			activeLock = new WeakReference<Lock>(lock);

			return lock;
		}
		else {
			return new ExclusiveLock();
		}
	}

	protected class ExclusiveLock extends AbstractLock {

		@Override
		protected void releaseLock() {
			releaseExclusiveLock();
		}
	}

	protected class ExclusiveDebugLock extends AbstractDebugLock {

		public ExclusiveDebugLock(Logger logger, boolean enableTrace) {
			super(logger, enableTrace);
		}

		@Override
		protected void releaseLock() {
			releaseExclusiveLock();
		}
	}

	private synchronized void releaseExclusiveLock() {
		lockInUse = false;
		activeLock = null;
		notifyAll();
	}
}
