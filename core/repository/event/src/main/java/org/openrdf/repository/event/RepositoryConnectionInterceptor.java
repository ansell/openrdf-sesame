/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.event;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;

/**
 * Interceptor interface for connection modification.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public interface RepositoryConnectionInterceptor {

	public abstract boolean close(RepositoryConnection conn);

	public abstract boolean begin(RepositoryConnection conn);

	public abstract boolean commit(RepositoryConnection conn);

	public abstract boolean rollback(RepositoryConnection conn);

	public abstract boolean add(RepositoryConnection conn, Resource subject, URI predicate, Value object,
			Resource... contexts);

	public abstract boolean remove(RepositoryConnection conn, Resource subject, URI predicate, Value object,
			Resource... contexts);

	public abstract boolean clear(RepositoryConnection conn, Resource... contexts);

	public abstract boolean setNamespace(RepositoryConnection conn, String prefix, String name);

	public abstract boolean removeNamespace(RepositoryConnection conn, String prefix);

	public abstract boolean clearNamespaces(RepositoryConnection conn);
}
