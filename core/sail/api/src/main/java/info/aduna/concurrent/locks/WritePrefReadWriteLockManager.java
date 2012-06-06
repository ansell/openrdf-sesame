/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.concurrent.locks;

/**
 * A read/write lock manager with writer preference. As soon as a write lock is
 * requested, this lock manager will block any read lock requests until the
 * writer's request has been satisfied.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class WritePrefReadWriteLockManager extends AbstractReadWriteLockManager {

	/*
	 * ----------- Variables -----------
	 */

	/**
	 * Flag indicating whether a write lock has been requested.
	 */
	private volatile boolean writeRequested = false;

	/*
	 * -------------- Constructors --------------
	 */

	/**
	 * Creates a MultiReadSingleWriteLockManager.
	 */
	public WritePrefReadWriteLockManager() {
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
	public WritePrefReadWriteLockManager(boolean trackLocks) {
		super(trackLocks);
	}

	/*
	 * --------- Methods ---------
	 */

	/**
	 * Gets a read lock, if available. This method will return <tt>null</tt> if
	 * the read lock is not immediately available.
	 */
	public Lock tryReadLock() {
		if (writeRequested || isWriterActive()) {
			return null;
		}
		synchronized (this) {
			if (isWriterActive()) {
				return null;
			}
	
			return createReadLock();
		}
	}

	/**
	 * Gets a read lock. This method blocks when a write lock is in use or has
	 * been requested until the write lock is released.
	 */
	public Lock getReadLock()
		throws InterruptedException
	{
		while (true) {
			Lock lock = tryReadLock();
			if (lock != null)
				return lock;
			waitForActiveWriter();
		}
	}

	/**
	 * Gets an exclusive write lock, if available. This method will return
	 * <tt>null</tt> if the write lock is not immediately available.
	 */
	public Lock tryWriteLock() {
		if (isWriterActive() || isReaderActive()) {
			return null;
		}
		synchronized (this) {
			if (isWriterActive() || isReaderActive()) {
				return null;
			}
	
			return createWriteLock();
		}
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
		writeRequested = true;
		try {
			// Wait for the write lock to be released
			while (isWriterActive()) {
				waitForActiveWriter();
			}
	
			// Wait for the read locks to be released
			while (isReaderActive()) {
				waitForActiveReaders();
			}
	
			return createWriteLock();
		} finally {
			writeRequested = false;
		}
	}
}
