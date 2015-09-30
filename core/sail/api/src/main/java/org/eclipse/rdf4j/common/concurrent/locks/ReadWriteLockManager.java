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
