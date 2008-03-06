/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.repository.http.HTTPRepository;

/**
 * A manager for {@link Repository}s that reside on a remote server. This
 * repository manager allows one to access repositories over HTTP similar to how
 * local repositories are accessed using the {@link LocalRepositoryManager}.
 * 
 * @author Arjohn Kampman
 */
public class RemoteRepositoryManager extends RepositoryManager {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The URL of the remote server, e.g. http://localhost:8080/openrdf-sesame/
	 */
	private String serverURL;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RepositoryManager that operates on the specfified base
	 * directory.
	 * 
	 * @param baseDir
	 *        The base directory where data for repositories can be stored, among
	 *        other things.
	 */
	public RemoteRepositoryManager(String serverURL) {
		super();
		this.serverURL = serverURL;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void initialize()
		throws RepositoryException
	{
		HTTPRepository systemRepository = new HTTPRepository(serverURL, SystemRepository.ID);
		systemRepository.initialize();

		repositories.put(SystemRepository.ID, systemRepository);
	}

	/**
	 * Gets the URL of the remote server, e.g.
	 * "http://localhost:8080/openrdf-sesame/".
	 */
	public String getServerURL() {
		return serverURL;
	}

	@Override
	public HTTPRepository getSystemRepository()
	{
		return (HTTPRepository)super.getSystemRepository();
	}

	/**
	 * Creates and initializes the repository with the specified ID.
	 * 
	 * @param id
	 *        A repository ID.
	 * @return The created repository, or <tt>null</tt> if no such repository
	 *         exists.
	 * @throws RepositoryConfigException
	 *         If no repository could be created due to invalid or incomplete
	 *         configuration data.
	 */
	@Override
	protected Repository createRepository(String id)
		throws RepositoryConfigException, RepositoryException
	{
		Repository systemRepository = getSystemRepository();

		RepositoryConnection con = systemRepository.getConnection();
		try {
			Repository repository = null;

			RepositoryConfig repConfig = RepositoryConfigUtil.getRepositoryConfig(systemRepository, id);
			if (repConfig != null) {
				repository = new HTTPRepository(serverURL, id);
				repository.initialize();
			}

			return repository;
		}
		finally {
			con.close();
		}
	}
}
