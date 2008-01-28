/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.event;

import org.openrdf.repository.Repository;

/**
 *
 * @author Herko ter Horst
 */
public interface InterceptingRepository extends Repository {

	/**
	 * Registers a <tt>RepositoryInterceptor</tt> that will receive
	 * notifications of operations that are performed on this repository.
	 */
	public void addRepositoryInterceptor(RepositoryInterceptor interceptor);

	/**
	 * Removes a registered <tt>RepositoryInterceptor</tt> from this
	 * repository.
	 */
	public void removeRepositoryInterceptor(RepositoryInterceptor interceptor);

}