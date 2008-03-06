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

	private static RepositoryRegistry sharedInstance;

	public static RepositoryRegistry getInstance() {
		if (sharedInstance == null) {
			sharedInstance = new RepositoryRegistry();
		}

		return sharedInstance;
	}

	public RepositoryRegistry() {
		super(RepositoryFactory.class);
	}

	protected String getKey(RepositoryFactory factory) {
		return factory.getRepositoryType();
	}
}
