/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.event;

import org.openrdf.repository.RepositoryConnection;

/**
 *
 * @author Herko ter Horst
 */
public interface InterceptingRepositoryConnection extends RepositoryConnection {

	/**
	 * Registers a <tt>RepositoryConnectionInterceptor</tt> that will receive
	 * notifications of operations that are performed on this connection.
	 */
	public void addRepositoryConnectionInterceptor(RepositoryConnectionInterceptor interceptor);

	/**
	 * Removes a registered <tt>RepositoryConnectionInterceptor</tt> from this
	 * connection.
	 */
	public void removeRepositoryConnectionInterceptor(RepositoryConnectionInterceptor interceptor);

}