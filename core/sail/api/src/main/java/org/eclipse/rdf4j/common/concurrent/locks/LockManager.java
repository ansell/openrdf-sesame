/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.common.concurrent.locks;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages a set of active locks. If any active lock is garbage collected it is
 * automatically removed from the set and logged.
 * 
 * @author James Leigh
 */
public class LockManager {

	/**
	 * Number of milliseconds to block thread before the garbage collection
	 * should search and collect abandoned active locks
	 */
	private static final int INITIAL_WAIT_TO_COLLECT = 10000;
	private static final int MAX_WAIT_TO_COLLECT = 90 * 60 * 1000;

	private static final AtomicLong seq = new AtomicLong();

	private static class WeakLockReference {

		String alias;

		String acquiredName;

		long acquiredId;

		Throwable stack;

		WeakReference<Lock> reference;
	}

	private final Logger logger = LoggerFactory.getLogger(LockManager.class);

	/**
	 * Controls whether the lock manager will keep a stack trace of where each
	 * lock was created. Mainly useful for debugging.
	 */
	private final boolean trackLocks;

	/**
	 * Number of milliseconds to block thread before the garbage collection
	 * should search and collect abandoned active locks
	 */
	private int waitToCollect;

	/**
	 * Set of active locks.
	 */
	private final Set<WeakLockReference> activeLocks = new HashSet<WeakLockReference>();

	/**
	 * Create a new set of locks.
	 */
	public LockManager() {
		this(false);
	}

	/**
	 * Creates a new set of locks, optionally with lock tracking enabled.
	 * 
	 * @param trackLocks
	 *        Controls whether to keep a stack trace of active locks. Enabling
	 *        lock tracking will add some overhead, but can be very useful for
	 *        debugging.
	 */
	public LockManager(boolean trackLocks) {
		this(trackLocks, INITIAL_WAIT_TO_COLLECT);
	}

	/**
	 * Creates a new set of locks, optionally with lock tracking enabled.
	 * 
	 * @param trackLocks
	 *        Controls whether to keep a stack trace of active locks. Enabling
	 *        lock tracking will add some overhead, but can be very useful for
	 *        debugging.
	 * @param collectionFrequency
	 *        Number of milliseconds to block the first thread, waiting for
	 *        active locks to finish, before running the memory garbage
	 *        collection, to free abandoned active locks.
	 */
	public LockManager(boolean trackLocks, int collectionFrequency) {
		this.trackLocks = trackLocks || Properties.lockTrackingEnabled();
		this.waitToCollect = collectionFrequency;
	}

	/**
	 * If any locks in this collection that are still active.
	 * 
	 * @return <code>true</code> of one or more locks that have not be released.
	 */
	public boolean isActiveLock() {
		synchronized (activeLocks) {
			return !activeLocks.isEmpty();
		}
	}

	/**
	 * Blocks current thread until the number of active locks has reached zero.
	 * 
	 * @throws InterruptedException
	 *         if any thread interrupted the current thread before or while the
	 *         current thread was waiting for a notification. The interrupted
	 *         status of the current thread is cleared when this exception is
	 *         thrown.
	 */
	public void waitForActiveLocks()
		throws InterruptedException
	{
		long now = -1;
		while (true) {
			boolean nochange;
			Set<WeakLockReference> before;
			synchronized (activeLocks) {
				if (activeLocks.isEmpty())
					return;
				before = new HashSet<WeakLockReference>(activeLocks);
				if (now < 0) {
					now = System.currentTimeMillis();
				}
				activeLocks.wait(waitToCollect);
				if (activeLocks.isEmpty())
					return;
				nochange = before.equals(activeLocks);
			}
			// guard against so-called spurious wakeup
			if (nochange && System.currentTimeMillis() - now >= waitToCollect / 2) {
				releaseAbandoned();
				now = -1;
			}
		}
	}

	/**
	 * Creates a new active lock. This increases the number of active locks until
	 * its {@link Lock#release()} method is called, which decreases the number of
	 * active locks by the same amount.
	 * 
	 * @param alias
	 *        a short string used to log abandon locks
	 * @return an active lock
	 */
	public synchronized Lock createLock(String alias) {
		final WeakLockReference weak = new WeakLockReference();
		weak.alias = alias;
		weak.acquiredName = Thread.currentThread().getName();
		weak.acquiredId = Thread.currentThread().getId();
		if (trackLocks) {
			weak.stack = new Throwable(alias + " lock " + seq.incrementAndGet() + " acquired in " + weak.acquiredName);
		}
		Lock lock = new Lock() {

			public synchronized boolean isActive() {
				synchronized (activeLocks) {
					return activeLocks.contains(weak);
				}
			}

			public synchronized void release() {
				synchronized (activeLocks) {
					if (activeLocks.remove(weak)) {
						activeLocks.notifyAll();
					}
				}
			}

			@Override
			public String toString() {
				if (weak.stack == null) {
					return weak.alias + " lock acquired in " + weak.acquiredName;
				} else {
					return weak.stack.getMessage();
				}
			}
		};
		weak.reference = new WeakReference<Lock>(lock);
		synchronized (activeLocks) {
			activeLocks.add(weak);
		}
		return lock;
	}

	private void releaseAbandoned() {
		System.gc();
		Thread.yield();
		synchronized (activeLocks) {
			if (!activeLocks.isEmpty()) {
				boolean stalled = true;
				Iterator<WeakLockReference> iter = activeLocks.iterator();
				while (iter.hasNext()) {
					WeakLockReference lock = iter.next();
					if (lock.reference.get() == null) {
						iter.remove();
						activeLocks.notifyAll();
						stalled = false;
						logAbandonedLock(lock);
					}
				}
				if (stalled) {
					// No active locks were found to be abandoned
					// wait longer next time before running gc
					if (waitToCollect < MAX_WAIT_TO_COLLECT) {
						waitToCollect = waitToCollect * 2;
					}
					logStalledLock(activeLocks);
				}
			}
		}
	}

	private void logAbandonedLock(WeakLockReference lock) {
		if (lock.stack == null && logger.isWarnEnabled()) {
			String msg = lock.alias
					+ " lock abandoned; lock was acquired in {}; consider setting the {} system property";
			logger.warn(msg, lock.acquiredName, Properties.TRACK_LOCKS);
		}
		else if (logger.isWarnEnabled()) {
			String msg = lock.alias + " lock abandoned; lock was acquired in " + lock.acquiredName;
			logger.warn(msg, lock.stack);
		}
	}

	private void logStalledLock(Collection<WeakLockReference> activeLocks) {
		Thread current = Thread.currentThread();
		if (activeLocks.size() == 1) {
			WeakLockReference lock = activeLocks.iterator().next();
			if (logger.isWarnEnabled()) {
				String msg = "Thread " + current.getName() + " is waiting on an active " + lock.alias
						+ " lock acquired in " + lock.acquiredName;
				if (lock.acquiredId == current.getId()) {
					if (lock.stack == null) {
						logger.warn(msg, new Throwable());
					}
					else {
						logger.warn(msg, new Throwable(lock.stack));
					}
				}
				else {
					if (lock.stack == null) {
						logger.info(msg);
					}
					else {
						logger.info(msg, new Throwable(lock.stack));
					}
				}
			}
		}
		else {
			String alias = null;
			boolean warn = false;
			for (WeakLockReference lock : activeLocks) {
				warn |= lock.acquiredId == current.getId();
				if (alias == null) {
					alias = lock.alias;
				}
				else if (!alias.contains(lock.alias)) {
					alias = alias + ", " + lock.alias;
				}
			}
			String msg = "Thread " + current.getName() + " is waiting on " + activeLocks.size() + " active "
					+ alias + " locks";
			if (warn) {
				logger.warn(msg);
			}
			else {
				logger.info(msg);
			}
		}
	}

}
