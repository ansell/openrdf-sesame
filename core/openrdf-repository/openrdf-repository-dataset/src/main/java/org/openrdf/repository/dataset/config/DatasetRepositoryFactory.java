/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.dataset.config;

import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.dataset.DatasetRepository;

/**
 * A {@link RepositoryFactory} that creates {@link DatasetRepository}s based on
 * RDF configuration data.
 * 
 * @author Arjohn Kampman
 */
public class DatasetRepositoryFactory implements RepositoryFactory {

	/**
	 * The type of repositories that are created by this factory.
	 * 
	 * @see RepositoryFactory#getRepositoryType()
	 */
	public static final String REPOSITORY_TYPE = "openrdf:DatasetRepository";

	/**
	 * Returns the repository's type: <tt>openrdf:DatasetRepository</tt>.
	 */
	public String getRepositoryType() {
		return REPOSITORY_TYPE;
	}

	public RepositoryImplConfig getConfig() {
		return new DatasetRepositoryConfig();
	}

	public Repository getRepository(RepositoryImplConfig config)
		throws RepositoryConfigException
	{
		if (config instanceof DatasetRepositoryConfig) {
			return new DatasetRepository();
		}

		throw new RepositoryConfigException("Invalid configuration class: " + config.getClass());
	}
}
