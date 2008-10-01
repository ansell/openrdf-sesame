/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

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
	 */
	Lock tryLock();

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