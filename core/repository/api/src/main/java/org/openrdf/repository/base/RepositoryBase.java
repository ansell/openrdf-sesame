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
package org.openrdf.repository.base;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;

/**
 * @author jeen
 */
public abstract class RepositoryBase implements Repository {

	private boolean initialized = false;

	private final Object initLock = new Object();

	public final void initialize()
		throws RepositoryException
	{
		if (!initialized) {
			synchronized (initLock) {
				if (!initialized) {
					initializeInternal();
					initialized = true;
				}
			}
		}
	}

	protected abstract void initializeInternal()
		throws RepositoryException;

	public final void shutDown()
		throws RepositoryException
	{
		synchronized (initLock) {
			shutDownInternal();
			initialized = false;
		}
	}

	public synchronized final boolean isInitialized() {
		return initialized;
	}

	protected abstract void shutDownInternal()
		throws RepositoryException;

}
