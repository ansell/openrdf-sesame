/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http.config;

import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.store.StoreConfigException;

/**
 * A {@link RepositoryFactory} that creates {@link HTTPRepository}s based on
 * RDF configuration data.
 * 
 * @author Arjohn Kampman
 */
public class HTTPRepositoryFactory implements RepositoryFactory {

	/**
	 * The type of repositories that are created by this factory.
	 * 
	 * @see RepositoryFactory#getRepositoryType()
	 */
	public static final String REPOSITORY_TYPE = "openrdf:HTTPRepository";

	/**
	 * Returns the repository's type: <tt>openrdf:HTTPRepository</tt>.
	 */
	public String getRepositoryType() {
		return REPOSITORY_TYPE;
	}

	public RepositoryImplConfig getConfig() {
		return new HTTPRepositoryConfig();
	}

	public Repository getRepository(RepositoryImplConfig config)
		throws StoreConfigException
	{
		HTTPRepository result = null;
		
		if (config instanceof HTTPRepositoryConfig) {
			HTTPRepositoryConfig httpConfig = (HTTPRepositoryConfig)config;
			result = new HTTPRepository(httpConfig.getURL());
			result.setSubjectSpace(httpConfig.getSubjectSpace());
//			result.setUsernameAndPassword(httpConfig.getUsername(), httpConfig.getPassword());
		}
		else {
			throw new StoreConfigException("Invalid configuration class: " + config.getClass());
		}
		return result;
	}
}
