/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.contextaware.config;

import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.contextaware.ContextAwareRepository;

/**
 * A {@link RepositoryFactory} that creates {@link ContextAwareRepository}s
 * based on RDF configuration data.
 * 
 * @author James Leigh
 */
public class ContextAwareFactory implements RepositoryFactory {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * The type of repositories that are created by this factory.
	 * 
	 * @see RepositoryFactory#getRepositoryType()
	 */
	public static final String REPOSITORY_TYPE = "openrdf:ContextAwareRepository";

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Returns the repository's type: <tt>openrdf:ContextAwareRepository</tt>.
	 */
	public String getRepositoryType() {
		return REPOSITORY_TYPE;
	}

	public RepositoryImplConfig getConfig() {
		return new ContextAwareConfig();
	}

	public Repository getRepository(RepositoryImplConfig configuration)
		throws RepositoryConfigException
	{
		if (configuration instanceof ContextAwareConfig) {
			ContextAwareConfig config = (ContextAwareConfig)configuration;

			ContextAwareRepository repo = new ContextAwareRepository();

			repo.setIncludeInferred(config.isIncludeInferred());
			repo.setMaxQueryTime(config.getMaxQueryTime());
			repo.setQueryLanguage(config.getQueryLanguage());
			repo.setBaseURI(config.getBaseURI());
			repo.setReadContexts(config.getReadContexts());
			repo.setAddContexts(config.getAddContexts());
			repo.setRemoveContexts(config.getRemoveContexts());
			repo.setArchiveContexts(config.getArchiveContexts());
			
			return repo;
		}

		throw new RepositoryConfigException("Invalid configuration class: " + configuration.getClass());
	}
}
