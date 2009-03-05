/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.event;

import org.openrdf.repository.Repository;

/**
 * @author Herko ter Horst
 */
public interface NotifyingRepository extends Repository {

	/**
	 * Registers a <tt>RepositoryListener</tt> that will receive notifications of
	 * operations that are performed on this repository.
	 */
	public void addRepositoryListener(RepositoryListener listener);

	/**
	 * Removes a registered <tt>RepositoryListener</tt> from this repository.
	 */
	public void removeRepositoryListener(RepositoryListener listener);

	/**
	 * Registers a <tt>RepositoryConnectionListener</tt> that will receive
	 * notifications of operations that are performed on any< connections that
	 * are created by this repository.
	 */
	public void addRepositoryConnectionListener(RepositoryConnectionListener listener);

	/**
	 * Removes a registered <tt>RepositoryConnectionListener</tt> from this
	 * repository.
	 */
	public void removeRepositoryConnectionListener(RepositoryConnectionListener listener);

}