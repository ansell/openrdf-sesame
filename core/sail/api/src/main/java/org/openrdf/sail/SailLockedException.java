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

/**
 * Indicates that a SAIL cannot be initialised because the configured persisted
 * location is locked.
 * 
 * @author James Leigh
 */
public class SailLockedException extends SailException {

	private static final long serialVersionUID = -2465202131214972460L;

	private String lockedBy;

	private String requestedBy;

	private LockManager manager;

	public SailLockedException(String requestedBy) {
		super("SAIL could not be locked (check permissions)");
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
