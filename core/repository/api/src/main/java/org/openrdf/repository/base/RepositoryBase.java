/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
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
