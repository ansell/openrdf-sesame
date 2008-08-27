/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import org.openrdf.StoreException;

public interface DelegatingRepositoryConnection extends RepositoryConnection {

	public RepositoryConnection getDelegate()
		throws StoreException;
	
	public void setDelegate(RepositoryConnection delegate);
}
