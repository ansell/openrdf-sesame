/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package org.openrdf.sail.rdbms.util;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.concurrent.locks.Lock;
import info.aduna.concurrent.locks.Properties;
import info.aduna.concurrent.locks.ReadWriteLockManager;

/**
 * A read/write lock manager with writer preference. As soon as a write lock is
 * requested, this lock manager will block any read lock requests until the
 * writer's request has been satisfied.
 * 
 * @author Arjohn Kampman
 */
public class WritePrefReadWriteLockManager implements ReadWriteLockManager {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Flag indicating whether a write lock has been requested.
	 */
	private boolean writeRequested;

	/**
	 * Flag indicating whether a writer is active.
	 */
	private boolean writerActive;

	/**
	 * Counter that keeps track of the numer of active read locks.
	 */
	private int activeReaders;

	/**
	 * Controls whether the lock manager will keep track of who owns the locks.
	 * Mainly useful for debugging.
	 */
	private boolean trackLocks;

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

	/**
	 * Creates a MultiReadSingleWriteLockManager.
	 */
	public WritePrefReadWriteLockManager() {
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
	public WritePrefReadWriteLockManager(boolean trackLocks) {
		writeRequested = false;
		writerActive = false;
		activeReaders = 0;

		this.trackLocks = trackLocks || Properties.lockTrackingEnabled();

		if (this.trackLocks) {
			readLocks = new LinkedList<WeakReference<Lock>>();
		}
	}

	/**
	 * Gets a read lock. This method blocks when a write lock is in use or has
	 * been requested until the write lock is released.
	 */
	public synchronized Lock getReadLock()
		throws InterruptedException
	{
		// Wait for any writing threads to finish
		while (writerActive || writeRequested) {
			wait();
		}

		// No candidates for writing anymore, go ahead
		activeReaders++;

		Lock lock = new AbstractLock(logger, trackLocks) {

			protected void releaseLock() {
				releaseReadLock(this);
			}
		};

		if (trackLocks) {
			readLocks.add(new WeakReference<Lock>(lock));
		}

		return lock;
	}

	/**
	 * Releases a read lock.
	 */
	private synchronized void releaseReadLock(Lock lock) {
		activeReaders--;

		if (trackLocks) {
			Iterator<WeakReference<Lock>> iter = readLocks.iterator();
			while (iter.hasNext()) {
				if (iter.next().get() == lock) {
					iter.remove();
					break;
				}
			}
		}

		if (activeReaders == 0) {
			// Maybe someone wants to write?
			notifyAll();
		}
	}

	/**
	 * Gets an exclusive write, lock if available. This method will return null if
	 * the write lock is in use or has already been requested. This method will
	 * return null if any read locks are active.
	 */
	public synchronized Lock tryWriteLock()
	{
		if (writeRequested) {
			// Someone else wants to write first
			return null;
		}

		writeRequested = true;

		// lock needs to be released
		if (writerActive || activeReaders > 0) {
			return null;
		}

		writerActive = true;
		writeRequested = false;
		notifyAll();

		Lock lock = new AbstractLock(logger, trackLocks) {

			protected void releaseLock() {
				releaseWriteLock();
			}
		};

		if (trackLocks) {
			writeLock = new WeakReference<Lock>(lock);
		}

		return lock;
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
		while (writeRequested) {
			// Someone else wants to write first
			wait();
		}

		writeRequested = true;

		// Wait for the lock to be released
		while (writerActive || activeReaders > 0) {
			wait();
		}

		writerActive = true;
		writeRequested = false;
		notifyAll();

		Lock lock = new AbstractLock(logger, trackLocks) {

			protected void releaseLock() {
				releaseWriteLock();
			}
		};

		if (trackLocks) {
			writeLock = new WeakReference<Lock>(lock);
		}

		return lock;
	}

	/**
	 * Release a write lock.
	 */
	private synchronized void releaseWriteLock() {
		writerActive = false;

		if (trackLocks) {
			writeLock = null;
		}

		notifyAll();
	}
}