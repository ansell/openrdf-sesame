/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.concurrent.locks;


/**
 * A read/write lock manager with reader preference. This lock manager block any
 * requests for write locks until all read locks have been released.
 * 
 * @author Arjohn Kampman
 */
public class ReadPrefReadWriteLockManager extends AbstractReadWriteLockManager {

	/*
	 * ----------- Variables -----------
	 */

	/**
	 * Counter that keeps track of the number of readers that are waiting for a
	 * read lock.
	 */
	private int waitingReaders = 0;

	/*
	 * -------------- Constructors --------------
	 */

	/**
	 * Creates a MultiReadSingleWriteLockManager.
	 */
	public ReadPrefReadWriteLockManager() {
		super();
	}

	/**
	 * Creates a new MultiReadSingleWriteLockManager, optionally with lock
	 * tracking enabled.
	 * 
	 * @param trackLocks
	 *        Controls whether the lock manager will keep track of active locks.
	 *        Enabling lock tracking will add some overhead, but can be very
	 *        useful for debugging.
	 */
	public ReadPrefReadWriteLockManager(boolean trackLocks) {
		super(trackLocks);
	}

	/*
	 * --------- Methods ---------
	 */

	/**
	 * Gets a read lock, if available. This method will return <tt>null</tt> if
	 * the read lock is not immediately available.
	 */
	public synchronized Lock tryReadLock() {
		if (writerActive) {
			return null;
		}

		return createReadLock();
	}

	/**
	 * Gets a read lock. This method blocks when a write lock is in use or has
	 * been requested until the write lock is released.
	 */
	public synchronized Lock getReadLock()
		throws InterruptedException
	{
		if (writerActive) {
			waitingReaders++;

			try {
				// Wait for the writer to finish
				while (writerActive) {
					wait();
				}
			}
			finally {
				waitingReaders--;
			}
		}

		return createReadLock();
	}

	/**
	 * Gets an exclusive write lock, if available. This method will return
	 * <tt>null</tt> if the write lock is not immediately available.
	 */
	public synchronized Lock tryWriteLock() {
		if (writerActive || activeReaders > 0 || waitingReaders > 0) {
			return null;
		}

		return createWriteLock();
	}

	/**
	 * Gets an exclusive write lock. This method blocks when the write lock is in
	 * use or has already been requested until the write lock is released. This
	 * method also block when read locks are active until all of them are
	 * released.
	 */
	public synchronized Lock getWriteLock()
		throws InterruptedException
	{
		while (writerActive || activeReaders > 0 || waitingReaders > 0) {
			// Wait for the lock to be released
			wait();
		}

		return createWriteLock();
	}
}
