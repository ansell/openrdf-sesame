/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.event.base;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.event.RepositoryConnectionListener;

/**
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class RepositoryConnectionListenerAdapter implements RepositoryConnectionListener {

	public void close(RepositoryConnection conn) {
	}

	public void begin(RepositoryConnection conn) {
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
}
