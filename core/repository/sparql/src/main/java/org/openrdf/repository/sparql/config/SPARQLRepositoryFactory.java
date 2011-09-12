/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sparql.config;

import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.sparql.SPARQLRepository;

/**
 * Creates {@link SPARQLRepository} from a configuration.
 * 
 * @author James Leigh
 */
public class SPARQLRepositoryFactory implements RepositoryFactory {

	public static final String REPOSITORY_TYPE = "openrdf:SPARQLRepository";

	public String getRepositoryType() {
		return REPOSITORY_TYPE;
	}

	public RepositoryImplConfig getConfig() {
		return new SPARQLRepositoryConfig();
	}

	public SPARQLRepository getRepository(RepositoryImplConfig config)
			throws RepositoryConfigException {
		SPARQLRepository result = null;

		if (config instanceof SPARQLRepositoryConfig) {
			SPARQLRepositoryConfig httpConfig = (SPARQLRepositoryConfig) config;
			result = new SPARQLRepository(httpConfig.getURL());
		} else {
			throw new RepositoryConfigException("Invalid configuration class: "
					+ config.getClass());
		}
		return result;
	}
}
