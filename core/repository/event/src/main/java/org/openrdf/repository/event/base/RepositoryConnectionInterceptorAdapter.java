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
import org.openrdf.repository.event.RepositoryConnectionInterceptor;

/**
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class RepositoryConnectionInterceptorAdapter implements RepositoryConnectionInterceptor {

	public boolean close(RepositoryConnection conn) {
		return false;
	}

	public boolean begin(RepositoryConnection conn) {
		return false;
	}

	public boolean commit(RepositoryConnection conn) {
		return false;
	}

	public boolean rollback(RepositoryConnection conn) {
		return false;
	}

	public boolean add(RepositoryConnection conn, Resource subject, URI predicate, Value object,
			Resource... contexts)
	{
		return false;
	}

	public boolean remove(RepositoryConnection conn, Resource subject, URI predicate, Value object,
			Resource... contexts)
	{
		return false;
	}

	public boolean clear(RepositoryConnection conn, Resource... contexts) {
		return false;
	}

	public boolean setNamespace(RepositoryConnection conn, String prefix, String name) {
		return false;
	}

	public boolean removeNamespace(RepositoryConnection conn, String prefix) {
		return false;
	}

	public boolean clearNamespaces(RepositoryConnection conn) {
		return false;
	}
}
