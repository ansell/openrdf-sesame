/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.concurrent.locks;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract base implementation of a read/write lock manager.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public abstract class AbstractReadWriteLockManager implements ReadWriteLockManager {

	/*
	 * ----------- Variables -----------
	 */

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Controls whether the lock manager will keep track of who owns the locks.
	 * Mainly useful for debugging.
	 */
	private final boolean trackLocks;

	/**
	 * Flag indicating whether a writer is active.
	 */
	protected boolean writerActive = false;;

	/**
	 * Counter that keeps track of the numer of active read locks.
	 */
	protected int activeReaders = 0;

	/**
	 * A list of references to currently active read lock. Only used when lock
	 * tracking is enabled.
	 */
	private List<WeakReference<Lock>> readLocks;

	/**
	 * A reference to currently active write lock, if any. Only used when lock
	 * tracking is enabled.
	 */
	@SuppressWarnings("unused")
	private WeakReference<Lock> writeLock;

	/*
	 * -------------- Constructors --------------
	 */

	/**
	 * Creates a MultiReadSingleWriteLockManager.
	 */
	public AbstractReadWriteLockManager() {
		this(false);
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
	public AbstractReadWriteLockManager(boolean trackLocks) {
		this.trackLocks = trackLocks || Properties.lockTrackingEnabled();

		if (this.trackLocks) {
			readLocks = new LinkedList<WeakReference<Lock>>();
		}
	}

	/*
	 * --------- Methods ---------
	 */

	/**
	 * Creates a new Lock for reading and increments counter for active readers.
	 * The lock is tracked if lock tracking is enabled. This method is not thread
	 * safe itself, the calling method is expected to handle synchronization
	 * issues.
	 * 
	 * @return a read lock.
	 */
	protected Lock createReadLock() {

		Lock lock = null;

		if (trackLocks) {
			lock = new ReadDebugLock(logger, true);
			readLocks.add(new WeakReference<Lock>(lock));
		}
		else {
			lock = new ReadLock();
		}
		activeReaders++;
		return lock;
	}

	protected class ReadLock extends AbstractLock {

		@Override
		protected void releaseLock() {
			releaseReadLock(this);
		}
	}

	protected class ReadDebugLock extends AbstractDebugLock {

		public ReadDebugLock(Logger logger, boolean enableTrace) {
			super(logger, enableTrace);
		}

		@Override
		protected void releaseLock() {
			releaseReadLock(this);
		}
	}

	/**
	 * Releases a read lock.
	 */
	private synchronized void releaseReadLock(Lock lock) {

		if (trackLocks) {
			Iterator<WeakReference<Lock>> iter = readLocks.iterator();
			while (iter.hasNext()) {
				if (iter.next().get() == lock) {
					iter.remove();
					break;
				}
			}
		}

		activeReaders--;

		if (activeReaders == 0) {
			// Maybe someone wants to write?
			notifyAll();
		}
	}

	/**
	 * Creates a new Lock for writing. The lock is tracked if lock tracking is
	 * enabled. This method is not thread safe itself for performance reasons,
	 * the calling method is expected to handle synchronization issues.
	 * 
	 * @return a write lock.
	 */
	protected Lock createWriteLock() {
		Lock lock;
		if (trackLocks) {
			lock = new WriteDebugLock(logger, true);
			writeLock = new WeakReference<Lock>(lock);
		}
		else {
			lock = new WriteLock();
		}
		writerActive = true;
		return lock;

	}

	protected class WriteLock extends AbstractLock {

		@Override
		protected void releaseLock() {
			releaseWriteLock();
		}
	}

	protected class WriteDebugLock extends AbstractDebugLock {

		public WriteDebugLock(Logger logger, boolean enableTrace) {
			super(logger, enableTrace);
		}

		@Override
		protected void releaseLock() {
			releaseWriteLock();
		}
	}

	/**
	 * Release a write lock.
	 */
	private synchronized void releaseWriteLock() {
		if (trackLocks) {
			writeLock = null;
		}
		writerActive = false;

		notifyAll();
	}
}
