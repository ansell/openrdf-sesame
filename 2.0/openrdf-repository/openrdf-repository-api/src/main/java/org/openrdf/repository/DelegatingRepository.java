/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

/**
 * Main interface for repositories that wrap another repository.
 */
public interface DelegatingRepository extends Repository {

	public Repository getDelegate();

	public void setDelegate(Repository delegate);
}
