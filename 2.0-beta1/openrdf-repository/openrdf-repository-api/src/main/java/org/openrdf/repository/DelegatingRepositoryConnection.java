/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

public interface DelegatingRepositoryConnection extends RepositoryConnection {

	public abstract RepositoryConnection getDelegate()
		throws RepositoryException;
}
