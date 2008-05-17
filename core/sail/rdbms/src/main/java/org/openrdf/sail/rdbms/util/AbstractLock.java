/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package org.openrdf.sail.rdbms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.concurrent.locks.Lock;
import info.aduna.concurrent.locks.Properties;

/**
 * An abstract implementation of the Lock interface. Subclasses only need to
 * implement the protected {@link #releaseLock()} method.
 * 
 * @author Arjohn Kampman
 */
abstract class AbstractLock implements Lock {

	protected Logger logger = null;

	/**
	 * Flag indicating whether this lock is active.
	 */
	protected boolean isActive = true;

	/**
	 * A stack trace used for tracing unreleased locks.
	 */
	protected Throwable creatorTrace;

	/**
	 * Creates a new lock.
	 */
	public AbstractLock() {
		this(null);
	}

	/**
	 * Creates a new lock.
	 */
	public AbstractLock(boolean enableTrace) {
		this(null, enableTrace);
	}

	/**
	 * Creates a new lock.
	 */
	public AbstractLock(Logger logger) {
		this(logger, false);
	}

	/**
	 * Creates a new lock, optionally with debug traces enabled.
	 * 
	 * @param enableTrace
	 *        Controls whether stack trace will be printed when unreleased locks
	 *        are garbage collected. Enabling trace will add some overhead to the
	 *        creation of locks.
	 */
	public AbstractLock(Logger logger, boolean enableTrace) {
		this.logger = logger;

		if (enableTrace) {
			// Keep track of where locks were acquired
			creatorTrace = new Throwable();
		}
	}

	protected Logger getLogger() {
		if (logger == null) {
			logger = LoggerFactory.getLogger(this.getClass());
		}

		return logger;
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
	 * This method is called when someone calls the <tt>release()</tt> method
	 * on an active lock. This method is called only once on an object as the
	 * <tt>release()</tt> method flags the lock as inactive after the first
	 * call to it.
	 */
	protected abstract void releaseLock();

	@Override
	protected void finalize()
		throws Throwable
	{
		if (isActive) {

			if (creatorTrace == null) {
				getLogger().warn(
						"Releasing active lock due to object destruction; consider setting the {} system property",
						Properties.TRACK_LOCKS);
			}
			else {
				getLogger().warn("Releasing active lock due to object destruction; lock was acquired in",
						creatorTrace);
			}

			try {
				releaseLock();
			}
			finally {
				isActive = false;
			}
		}

		super.finalize();
	}
}