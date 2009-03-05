/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager.util;

import org.openrdf.repository.manager.RepositoryManager;

public interface RepositoryManagerListener {

	public void initialized(RepositoryManager manager);

	public void refreshed(RepositoryManager manager);

	public void shutDown(RepositoryManager manager);
}
