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
package org.openrdf.sail;

import java.util.concurrent.ExecutionException;

import info.aduna.concurrent.locks.Lock;


/**
 *
 * @author james
 */
public interface LockManager {

	/**
	 * Technical description of where the lock is located, such as a URL.
	 */
	String getLocation();

	/**
	 * Determines if the SAIL is locked.
	 * 
	 * @return <code>true</code> if the SAIL is already locked.
	 */
	boolean isLocked();

	/**
	 * Creates a lock in a SAIL if it does not yet exist.
	 * 
	 * @return a newly acquired lock or null if the SAIL is already locked.
	 * @throws InterruptedException 
	 */
	Lock tryLock() throws InterruptedException;

	/**
	 * Creates a lock in a SAIL if it does not yet exist.
	 * 
	 * @return a newly acquired lock.
	 * @throws SailLockedException
	 *             if the directory is already locked.
	 */
	Lock lockOrFail() throws SailLockedException;

	/**
	 * Revokes a lock owned by another process.
	 * 
	 * @return <code>true</code> if a lock was successfully revoked.
	 */
	boolean revokeLock();

}