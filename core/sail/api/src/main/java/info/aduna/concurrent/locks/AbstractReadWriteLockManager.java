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

package info.aduna.concurrent.locks;


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

	/**
	 * Flag indicating whether a writer is active.
	 */
	private final LockManager activeWriter;

	/**
	 * Counter that keeps track of the numer of active read locks.
	 */
	private final LockManager activeReaders;

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
		boolean trace = trackLocks || Properties.lockTrackingEnabled();
		activeWriter = new LockManager(trace);
		activeReaders = new LockManager(trace);
	}

	/*
	 * --------- Methods ---------
	 */

	/**
	 * If a writer is active
	 */
	protected boolean isWriterActive() {
		return activeWriter.isActiveLock();
	}

	/**
	 * If one or more readers are active
	 */
	protected boolean isReaderActive() {
		return activeReaders.isActiveLock();
	}

	/**
	 * Blocks current thread until after the writer lock is released (if active).
	 * 
	 * @throws InterruptedException
	 */
	protected void waitForActiveWriter()
		throws InterruptedException
	{
		activeWriter.waitForActiveLocks();
	}

	/**
	 * Blocks current thread until there are no reader locks active.
	 * 
	 * @throws InterruptedException
	 */
	protected void waitForActiveReaders()
		throws InterruptedException
	{
		activeReaders.waitForActiveLocks();
	}

	/**
	 * Creates a new Lock for reading and increments counter for active readers.
	 * The lock is tracked if lock tracking is enabled. This method is not thread
	 * safe itself, the calling method is expected to handle synchronization
	 * issues.
	 * 
	 * @return a read lock.
	 */
	protected Lock createReadLock() {
		return activeReaders.createLock("Read");
	}

	/**
	 * Creates a new Lock for writing. The lock is tracked if lock tracking is
	 * enabled. This method is not thread safe itself for performance reasons,
	 * the calling method is expected to handle synchronization issues.
	 * 
	 * @return a write lock.
	 */
	protected Lock createWriteLock() {
		return activeWriter.createLock("Write");
	}
}
