/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.event;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.repository.RepositoryConnection;

/**
 * Interceptor interface for connection modification.
 * 
 * @author Herko ter Horst
 */
public interface RepositoryConnectionInterceptor {

	public abstract boolean close(RepositoryConnection conn);

	public abstract boolean setAutoCommit(RepositoryConnection conn, boolean autoCommit);

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

	public abstract boolean execute(RepositoryConnection conn, QueryLanguage ql, String update,
			String baseURI, Update operation);
}
