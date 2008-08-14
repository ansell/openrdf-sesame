/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

/**
 * Indicates that a Repository cannot be initialised because the configured persisted
 * location is locked.
 * 
 * @author James Leigh
 */
public class RepositoryLockedException extends RepositoryException {
	private static final long serialVersionUID = -1544864578935422866L;
	private String lockedBy;
	private String requestedBy;

	public RepositoryLockedException(String lockedBy, String requestedBy, String msg, Throwable t) {
		super(msg, t);
		this.lockedBy = lockedBy;
		this.requestedBy = requestedBy;
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

}
