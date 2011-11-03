/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.concurrent.locks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of {@link AbstractLock} adding a finalizer and debug tracing for
 * unreleased locks.
 * 
 * @author Arjohn Kampman
 */
abstract class AbstractDebugLock extends AbstractLock {

	private Logger logger = null;

	/**
	 * A stack trace used for tracing unreleased locks.
	 */
	private Throwable creatorTrace;

	/**
	 * Creates a new lock.
	 */
	public AbstractDebugLock() {
		this(null);
	}

	/**
	 * Creates a new lock.
	 */
	public AbstractDebugLock(boolean enableTrace) {
		this(null, enableTrace);
	}

	/**
	 * Creates a new lock.
	 */
	public AbstractDebugLock(Logger logger) {
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
	public AbstractDebugLock(Logger logger, boolean enableTrace) {
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

	@Override
	protected void finalize()
		throws Throwable
	{
		if (isActive()) {
			if (creatorTrace == null) {
				getLogger().warn(
						"Releasing active lock due to object destruction; consider setting the {} system property",
						Properties.TRACK_LOCKS);
			}
			else {
				getLogger().warn("Releasing active lock due to object destruction; lock was acquired in",
						creatorTrace);
			}

			release();
		}

		super.finalize();
	}
}
