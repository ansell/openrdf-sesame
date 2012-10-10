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
import org.openrdf.repository.event.RepositoryConnectionInterceptor;

/**
 * @author Herko ter Horst
 */
public class RepositoryConnectionInterceptorAdapter implements RepositoryConnectionInterceptor {

	public boolean add(RepositoryConnection conn, Resource subject, URI predicate, Value object,
			Resource... contexts)
	{
		return false;
	}

	public boolean begin(RepositoryConnection conn) {
		return false;
	}
	
	public boolean clear(RepositoryConnection conn, Resource... contexts) {
		return false;
	}

	public boolean clearNamespaces(RepositoryConnection conn) {
		return false;
	}

	public boolean close(RepositoryConnection conn) {
		return false;
	}

	public boolean commit(RepositoryConnection conn) {
		return false;
	}

	public boolean remove(RepositoryConnection conn, Resource subject, URI predicate, Value object,
			Resource... contexts)
	{
		return false;
	}

	public boolean removeNamespace(RepositoryConnection conn, String prefix) {
		return false;
	}

	public boolean rollback(RepositoryConnection conn) {
		return false;
	}

	@Deprecated
	public boolean setAutoCommit(RepositoryConnection conn, boolean autoCommit) {
		return false;
	}

	public boolean setNamespace(RepositoryConnection conn, String prefix, String name) {
		return false;
	}

	public boolean execute(RepositoryConnection conn, QueryLanguage ql, String update, String baseURI,
			Update operation)
	{
		return false;
	}


}
