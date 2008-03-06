/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import org.openrdf.repository.base.RepositoryConnectionBase;

/**
 * Abstract class implementing most 'convenience' methods in the
 * RepositoryConnection interface by transforming parameters and mapping the
 * methods to the basic (abstractly declared) methods.
 * 
 * @deprecated This class has been removed from the main Repository API package,
 *             use {@link RepositoryConnectionBase} instead.
 */
@Deprecated
public abstract class AbstractRepositoryConnection extends RepositoryConnectionBase
{

	/**
	 * @param repository
	 */
	protected AbstractRepositoryConnection(Repository repository) {
		super(repository);
	}
}
