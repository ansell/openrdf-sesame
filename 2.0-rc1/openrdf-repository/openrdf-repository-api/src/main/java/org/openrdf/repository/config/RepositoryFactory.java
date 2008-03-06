/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

import org.openrdf.repository.Repository;

/**
 * A RepositoryFactory takes care of creating and initializing a specific type
 * of {@link Repository}s based on RDF configuration data. RepositoryFactory's
 * are used by the {@link RepositoryManager} to create specific repositories and
 * to initialize them based on the configuration data that it manages, for
 * example in a server environment.
 * 
 * @author Arjohn Kampman
 */
public interface RepositoryFactory {

	/**
	 * Returns the type of the repositories that this factory creates. Repository
	 * types are used for identification and should uniquely identify specific
	 * implementations of the Repository API. This type <em>can</em> be equal
	 * to the fully qualified class name of the repository, but this is not
	 * required.
	 */
	public String getRepositoryType();

	public RepositoryImplConfig getConfig();

	/**
	 * Returns a Repository instance that has been initialized using the supplied
	 * configuration data.
	 * @param config TODO
	 * 
	 * @return The created (but un-initialized) repository.
	 * @throws RepositoryConfigException
	 *         If no repository could be created due to invalid or incomplete
	 *         configuration data.
	 */
	public Repository getRepository(RepositoryImplConfig config)
		throws RepositoryConfigException;
}
