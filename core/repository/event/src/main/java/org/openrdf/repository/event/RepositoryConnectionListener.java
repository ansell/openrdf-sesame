/*
 * Copyright James Leigh (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.event;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;

/**
 * Listener interface for connection modification.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public interface RepositoryConnectionListener {

	public abstract void close(RepositoryConnection conn);

	public abstract void begin(RepositoryConnection conn);

	public abstract void commit(RepositoryConnection conn);

	public abstract void rollback(RepositoryConnection conn);

	public abstract void add(RepositoryConnection conn, Resource subject, URI predicate, Value object,
			Resource... contexts);

	public abstract void remove(RepositoryConnection conn, Resource subject, URI predicate, Value object,
			Resource... contexts);

	public abstract void clear(RepositoryConnection conn, Resource... contexts);

	public abstract void setNamespace(RepositoryConnection conn, String prefix, String name);

	public abstract void removeNamespace(RepositoryConnection conn, String prefix);

	public abstract void clearNamespaces(RepositoryConnection conn);
}
