/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

/**
 * @author Herko ter Horst
 */
public interface DelegatingRepositoryImplConfig extends RepositoryImplConfig {

	public RepositoryImplConfig getDelegate();
}