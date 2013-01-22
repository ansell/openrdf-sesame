/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
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
