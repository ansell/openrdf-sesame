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
 * Interceptor interface for Repository state changes.
 * 
 * @author Herko ter Horst
 * 
 */
public interface RepositoryInterceptor extends EventListener {

	public abstract boolean getConnection(Repository repo, RepositoryConnection conn);

	public abstract boolean initialize(Repository repo);

	public abstract boolean setDataDir(Repository repo, File dataDir);

	public abstract boolean shutDown(Repository repo);

}
