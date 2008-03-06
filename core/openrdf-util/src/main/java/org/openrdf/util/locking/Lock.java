/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.locking;

/**
 * A lock on a specific monitor that can be used for synchronization purposes.
 */
public interface Lock {

	/**
	 * Checks whether the lock is still active.
	 */
	public boolean isActive();

	/**
	 * Release the lock, making it inactive.
	 */
	public void release();
}
