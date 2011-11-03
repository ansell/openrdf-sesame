/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.concurrent.locks;

/**
 * A lock manager that manages a multi-read, single-write lock. This lock
 * manager allows multiple read locks to be active at the same time. The write
 * lock is exclusive, meaning that no other read- or write locks may be active
 * at the same time.
 * 
 * @author Arjohn Kampman
 */
public interface ReadWriteLockManager {

	/**
	 * Gets a read lock, if available. This method will return <tt>null</tt> if
	 * the read lock is not immediately available.
	 */
	public Lock tryReadLock();

	/**
	 * Gets a read lock. This method blocks until the read lock is available.
	 * 
	 * @throws InterruptedException
	 *         In case the thread requesting the lock was
	 *         {@link Thread#interrupt() interrupted}.
	 */
	public Lock getReadLock()
		throws InterruptedException;

	/**
	 * Gets an exclusive write lock, if available. This method will return
	 * <tt>null</tt> if the write lock is not immediately available.
	 */
	public Lock tryWriteLock();

	/**
	 * Gets an exclusive write lock. This method blocks until the write lock is
	 * available.
	 * 
	 * @throws InterruptedException
	 *         In case the thread requesting the lock was
	 *         {@link Thread#interrupt() interrupted}.
	 */
	public Lock getWriteLock()
		throws InterruptedException;
}
