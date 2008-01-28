/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

import info.aduna.lang.service.ServiceRegistry;

/**
 * A registry that keeps track of the available {@link RepositoryFactory}s.
 * 
 * @author Arjohn Kampman
 */
public class RepositoryRegistry extends ServiceRegistry<String, RepositoryFactory> {

	private static RepositoryRegistry defaultRegistry;

	/**
	 * Gets the default QueryParserRegistry.
	 * 
	 * @return The default registry.
	 */
	public static synchronized RepositoryRegistry getInstance() {
		if (defaultRegistry == null) {
			defaultRegistry = new RepositoryRegistry();
		}

		return defaultRegistry;
	}

	public RepositoryRegistry() {
		super(RepositoryFactory.class);
	}

	@Override
	protected String getKey(RepositoryFactory factory) {
		return factory.getRepositoryType();
	}
}
