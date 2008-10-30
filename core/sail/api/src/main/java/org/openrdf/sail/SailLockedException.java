/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

import org.openrdf.store.StoreException;

/**
 * Indicates that a SAIL cannot be initialised because the configured persisted
 * location is locked.
 * 
 * @author James Leigh
 */
public class SailLockedException extends StoreException {

	private static final long serialVersionUID = -2465202131214972460L;

	private String lockedBy;

	private String requestedBy;

	private LockManager manager;

	public SailLockedException(String requestedBy) {
		super("SAIL is already locked");
		this.requestedBy = requestedBy;
	}

	public SailLockedException(String lockedBy, String requestedBy) {
		super("SAIL is already locked by: " + lockedBy);
		this.lockedBy = lockedBy;
		this.requestedBy = requestedBy;
	}

	public SailLockedException(String lockedBy, String requestedBy, LockManager manager) {
		super("SAIL is already locked by: " + lockedBy + " in " + manager.getLocation());
		this.lockedBy = lockedBy;
		this.requestedBy = requestedBy;
		this.manager = manager;
	}

	/**
	 * Returns the name representing the Java virtual machine that acquired the
	 * lock.
	 * 
	 * @return the name representing the Java virtual machine that acquired the
	 *         lock.
	 */
	public String getLockedBy() {
		return lockedBy;
	}

	/**
	 * Returns the name representing the Java virtual machine that requested the
	 * lock.
	 * 
	 * @return the name representing the Java virtual machine that requested the
	 *         lock.
	 */
	public String getRequestedBy() {
		return requestedBy;
	}

	/**
	 * @return Returns the lock manager that failed to obtain a lock.
	 */
	public LockManager getLockManager() {
		return manager;
	}
}
