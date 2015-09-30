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
package org.eclipse.rdf4j.sail;

import org.eclipse.rdf4j.common.concurrent.locks.Lock;


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