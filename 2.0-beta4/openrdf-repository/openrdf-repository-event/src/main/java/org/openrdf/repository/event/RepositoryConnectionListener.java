/*
 * Copyright James Leigh (c) 2007.
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
 * @author James Leigh <james@leighnet.ca>
 *
 */
public interface RepositoryConnectionListener {

	public abstract void add(RepositoryConnection conn, Resource subject, URI predicate, Value object, Resource... contexts);

	public abstract void clear(RepositoryConnection conn, Resource... contexts);

	public abstract void remove(RepositoryConnection conn, Resource subject, URI predicate, Value object, Resource... contexts);

	public abstract void removeNamespace(RepositoryConnection conn, String prefix);

	public abstract void setNamespace(RepositoryConnection conn, String prefix, String name);

	public abstract void close(RepositoryConnection conn);

	public abstract void commit(RepositoryConnection conn);

	public abstract void rollback(RepositoryConnection conn);

	public abstract void setAutoCommit(RepositoryConnection conn, boolean autoCommit);
}
