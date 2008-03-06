/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigUtil;

/**
 * A manager for {@link Repository}s. Every <tt>RepositoryManager</tt> has
 * one SYSTEM repository and zero or more "user repositories". The SYSTEM
 * repository contains data that describes the configuration of the other
 * repositories (their IDs, which implementations of the Repository API to use,
 * access rights, etc.). The other repositories are instantiated based on this
 * configuration data.
 * 
 * @author Arjohn Kampman
 */
public abstract class RepositoryManager {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*-----------*
	 * Variables *
	 *-----------*/

	protected Map<String, Repository> repositories;

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
	public RepositoryManager() {
		this.repositories = new HashMap<String, Repository>();
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Initializes the repository manager.
	 */
	public abstract void initialize()
		throws RepositoryException;

	public Repository getSystemRepository() {
		return repositories.get(SystemRepository.ID);
	}

	public Set<String> getRepositoryIDs()
		throws RepositoryException, RepositoryConfigException
	{
		return RepositoryConfigUtil.getRepositoryIDs(getSystemRepository());
	}

	public boolean hasRepositoryConfig(String repositoryID)
		throws RepositoryException, RepositoryConfigException
	{
		return RepositoryConfigUtil.hasRepositoryConfig(getSystemRepository(), repositoryID);
	}

	public RepositoryConfig getRepositoryConfig(String repositoryID)
		throws RepositoryConfigException, RepositoryException
	{
		return RepositoryConfigUtil.getRepositoryConfig(getSystemRepository(), repositoryID);
	}

	/**
	 * Adds or updates the configuration of a repository to the manager's system
	 * repository. The system repository may already contain a configuration for
	 * a repository with the same ID as specified by <tt>config</tt>, in which
	 * case all previous configuration data for that repository will be cleared
	 * before the new configuration is added.
	 * 
	 * @param config
	 *        The repository configuration that should be added to or updated in
	 *        the system repository.
	 * @throws RepositoryException
	 *         If the manager failed to update it's system repository.
	 * @throws RepositoryConfigException
	 *         If the manager doesn't know how to update a configuration due to
	 *         inconsistent configuration data in the system repository. For
	 *         example, this happens when there are multiple existing
	 *         configurations with the concerning ID.
	 */
	public void addRepositoryConfig(RepositoryConfig config)
		throws RepositoryException, RepositoryConfigException
	{
		RepositoryConfigUtil.updateRepositoryConfigs(getSystemRepository(), config);
	}

	/**
	 * Removes the configuration for the specified repository from the manager's
	 * system repository if such a configuration is present. Returns
	 * <tt>true</tt> if the system repository actually contained the specified
	 * repository configuration.
	 * 
	 * @param repositoryID
	 *        The ID of the repository whose configuration needs to be removed.
	 * @throws RepositoryException
	 *         If the manager failed to update it's system repository.
	 * @throws RepositoryConfigException
	 *         If the manager doesn't know how to remove a configuration due to
	 *         inconsistent configuration data in the system repository. For
	 *         example, this happens when there are multiple existing
	 *         configurations with the concerning ID.
	 */
	public boolean removeRepositoryConfig(String repositoryID)
		throws RepositoryException, RepositoryConfigException
	{
		boolean wasRemoved = RepositoryConfigUtil.removeRepositoryConfigs(getSystemRepository(), repositoryID);

		if (wasRemoved) {
			Repository repository = null;

			synchronized (repositories) {
				repository = repositories.remove(repositoryID);
			}

			if (repository != null) {
				repository.shutDown();
			}
		}

		return wasRemoved;
	}

	/**
	 * Gets the repository that is known by the specified ID from this manager.
	 * 
	 * @param id
	 *        A repository ID.
	 * @return A Repository object, or <tt>null</tt> if no repository was known
	 *         for the specified ID.
	 * @throws RepositoryConfigException
	 *         If no repository could be created due to invalid or incomplete
	 *         configuration data.
	 */
	public Repository getRepository(String id)
		throws RepositoryConfigException, RepositoryException
	{
		synchronized (repositories) {
			Repository repository = repositories.get(id);

			if (repository == null) {
				// First call, create and initialize the repository.
				repository = createRepository(id);

				if (repository != null) {
					repositories.put(id, repository);
				}
			}

			return repository;
		}
	}

	/**
	 * Returns all inititalized repositories. This method returns fast as no lazy
	 * creation of repositories takes place.
	 * 
	 * @return An unmodifiable collection containing the initialized
	 *         repositories.
	 * @see #getAllRepositories()
	 */
	public Collection<Repository> getInitializedRepositories() {
		synchronized (repositories) {
			return new ArrayList<Repository>(repositories.values());
		}
	}

	/**
	 * Returns all configured repositories. This may be an expensive operation as
	 * it initializes repositories that have not been initialized yet.
	 * 
	 * @return The Set of all Repositories defined in the SystemRepository.
	 * @see #getInitializedRepositories()
	 */
	public Collection<Repository> getAllRepositories()
		throws RepositoryConfigException, RepositoryException
	{
		Set<String> idSet = getRepositoryIDs();

		ArrayList<Repository> result = new ArrayList<Repository>(idSet.size());

		for (String id : idSet) {
			result.add(getRepository(id));
		}

		return result;
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
	protected abstract Repository createRepository(String id)
		throws RepositoryConfigException, RepositoryException;

	/**
	 * Shuts down all initialized user repositories.
	 * 
	 * @see #shutDown()
	 */
	public void refresh() {
		Iterator<Map.Entry<String, Repository>> iter = repositories.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Repository> entry = iter.next();
			String repositoryID = entry.getKey();
			Repository repository = entry.getValue();

			if (!SystemRepository.ID.equals(repositoryID)) {
				iter.remove();

				try {
					repository.shutDown();
				}
				catch (RepositoryException e) {
					logger.error("Failed to shut down repository", e);
				}
			}
		}
	}

	/**
	 * Shuts down all initialized repositories, including the SYSTEM repository.
	 * 
	 * @see #refresh()
	 */
	public void shutDown() {
		synchronized (repositories) {
			for (Repository repository : repositories.values()) {
				try {
					repository.shutDown();
				}
				catch (RepositoryException e) {
					logger.error("Repository shut down failed", e);
				}
			}

			repositories = new HashMap<String, Repository>();
		}
	}
}
