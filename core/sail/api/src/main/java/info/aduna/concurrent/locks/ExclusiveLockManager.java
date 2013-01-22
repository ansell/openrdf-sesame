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
 * A lock manager for exclusive locks.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class ExclusiveLockManager {

	/*
	 * ----------- Variables -----------
	 */

	private final LockManager lock;

	/*
	 * -------------- Constructors --------------
	 */

	/**
	 * Creates an ExclusiveLockManager.
	 */
	public ExclusiveLockManager() {
		this(false);
	}

	/**
	 * Creates an ExclusiveLockManager.
	 * 
	 * @param name
	 *        Common name used for debugging
	 * @param trackLocks
	 *        If create stack traces should be logged
	 */
	public ExclusiveLockManager(boolean trackLocks) {
		this.lock = new LockManager(trackLocks || Properties.lockTrackingEnabled());
	}

	/*
	 * --------- Methods ---------
	 */

	/**
	 * Gets the exclusive lock, if available. This method will return
	 * <tt>null</tt> if the exclusive lock is not immediately available.
	 */
	public Lock tryExclusiveLock() {
		if (lock.isActiveLock()) {
			return null;
		}
		synchronized (this) {
			if (lock.isActiveLock()) {
				return null;
			}
	
			return createLock();
		}
	}

	/**
	 * Gets the exclusive lock. This method blocks when the exclusive lock is
	 * currently in use until it is released.
	 */
	public synchronized Lock getExclusiveLock()
		throws InterruptedException
	{
		while (lock.isActiveLock()) {
			// Someone else currently has the lock
			lock.waitForActiveLocks();
		}

		return createLock();
	}

	private Lock createLock() {
		return lock.createLock("Exclusive");
	}
}
