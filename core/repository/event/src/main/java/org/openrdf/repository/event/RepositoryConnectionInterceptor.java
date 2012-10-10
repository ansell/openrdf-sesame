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

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean close(RepositoryConnection conn);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean begin(RepositoryConnection conn);

	/**
	 * @deprecated since 2.7.0. Use {@link #begin(RepositoryConnection)} instead.
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @param autoCommit
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	@Deprecated
	public abstract boolean setAutoCommit(RepositoryConnection conn, boolean autoCommit);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean commit(RepositoryConnection conn);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean rollback(RepositoryConnection conn);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean add(RepositoryConnection conn, Resource subject, URI predicate, Value object,
			Resource... contexts);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean remove(RepositoryConnection conn, Resource subject, URI predicate, Value object,
			Resource... contexts);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean clear(RepositoryConnection conn, Resource... contexts);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean setNamespace(RepositoryConnection conn, String prefix, String name);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean removeNamespace(RepositoryConnection conn, String prefix);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean clearNamespaces(RepositoryConnection conn);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean execute(RepositoryConnection conn, QueryLanguage ql, String update,
			String baseURI, Update operation);
}
