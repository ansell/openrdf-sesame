/*
 * Copyright James Leigh (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.event;

import java.io.File;
import java.util.EventListener;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

/**
 * Listener interface for Repository state changes.
 * 
 * @author James Leigh
 * 
 */
public interface RepositoryListener extends EventListener {

	public abstract void getConnection(Repository repo, RepositoryConnection conn);

	public abstract void initialize(Repository repo);

	public abstract void setDataDir(Repository repo, File dataDir);

	public abstract void shutDown(Repository repo);

}
