/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.event.base;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.event.RepositoryConnectionListener;

/**
 * @author Herko ter Horst
 */
public class RepositoryConnectionListenerAdapter implements RepositoryConnectionListener {

	public void close(RepositoryConnection conn) {
	}

	public void setAutoCommit(RepositoryConnection conn, boolean autoCommit) {
	}

	public void commit(RepositoryConnection conn) {
	}

	public void rollback(RepositoryConnection conn) {
	}

	public void add(RepositoryConnection conn, Resource subject, URI predicate, Value object,
			Resource... contexts)
	{
	}

	public void remove(RepositoryConnection conn, Resource subject, URI predicate, Value object,
			Resource... contexts)
	{
	}

	public void clear(RepositoryConnection conn, Resource... contexts) {
	}

	public void setNamespace(RepositoryConnection conn, String prefix, String name) {
	}

	public void removeNamespace(RepositoryConnection conn, String prefix) {
	}

	public void clearNamespaces(RepositoryConnection conn) {
	}

	public void execute(RepositoryConnection conn, QueryLanguage ql, String update, String baseURI,
			Update operation)
	{
	}
}
