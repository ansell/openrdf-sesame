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
