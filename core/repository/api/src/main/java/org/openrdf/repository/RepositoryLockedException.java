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
package org.openrdf.repository;

/**
 * Indicates that a Repository cannot be initialised because the configured
 * persisted location is locked.
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
