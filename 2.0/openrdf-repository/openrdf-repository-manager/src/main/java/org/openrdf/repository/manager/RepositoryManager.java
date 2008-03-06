/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
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

	private final Map<String, Repository> repositories;

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
	 * 
	 * @throws RepositoryException
	 *         If the manager failed to initialize the SYSTEM repository.
	 */
	public void initialize()
		throws RepositoryException
	{
		Repository systemRepository = createSystemRepository();

		synchronized (repositories) {
			repositories.put(SystemRepository.ID, systemRepository);
		}
	}

	protected abstract Repository createSystemRepository()
		throws RepositoryException;

	/**
	 * Gets the SYSTEM repository.
	 */
	public Repository getSystemRepository() {
		synchronized (repositories) {
			return repositories.get(SystemRepository.ID);
		}
	}

	public Set<String> getRepositoryIDs()
		throws RepositoryException
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
		logger.info("Removing repository configuration for {}.", repositoryID);
		boolean isRemoved = false;

		synchronized (repositories) {
			isRemoved = RepositoryConfigUtil.removeRepositoryConfigs(getSystemRepository(), repositoryID);

			if (isRemoved) {
				logger.debug("Shutdown repository {} after removal of configuration.", repositoryID);
				Repository repository = repositories.remove(repositoryID);

				if (repository != null) {
					repository.shutDown();
				}
			}
		}

		return isRemoved;
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
			Repository result = repositories.get(id);

			if (result == null) {
				// First call, create and initialize the repository.
				result = createRepository(id);

				if (result != null) {
					repositories.put(id, result);
				}
			}

			return result;
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
	 * Gets the repository that is known by the specified ID from this manager.
	 * 
	 * @param id
	 *        A repository ID.
	 * @return A Repository object, or <tt>null</tt> if no repository was known
	 *         for the specified ID.
	 * @throws RepositoryException
	 *         When not able to retrieve existing configurations
	 */
	public abstract RepositoryInfo getRepositoryInfo(String id)
		throws RepositoryException;

	public Collection<RepositoryInfo> getAllRepositoryInfos()
		throws RepositoryException
	{
		return getAllRepositoryInfos(false);
	}

	public Collection<RepositoryInfo> getAllUserRepositoryInfos()
		throws RepositoryException
	{
		return getAllRepositoryInfos(true);
	}

	/**
	 * 
	 * @param skipSystemRepo
	 * @throws RepositoryException
	 *         When not able to retrieve existing configurations
	 */
	public abstract Collection<RepositoryInfo> getAllRepositoryInfos(boolean skipSystemRepo)
		throws RepositoryException;

	/**
	 * Shuts down all initialized user repositories.
	 * 
	 * @see #shutDown()
	 */
	public void refresh() {
		logger.debug("Refreshing repository information in manager...");
		synchronized (repositories) {
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

					// FIXME: data dirs of removed but uninitialized repositories are
					// not cleaned up
					try {
						RepositoryConnection con = getSystemRepository().getConnection();
						try {
							if (RepositoryConfigUtil.getContext(con, repositoryID) == null) {
								logger.info("Cleaning up repository {}, its configuration has been removed",
										repositoryID);

								cleanUpRepository(repositoryID);
							}
						}
						finally {
							con.close();
						}
					}
					catch (RepositoryException e) {
						logger.error("Failed to process repository configuration changes", e);
					}
					catch (RepositoryConfigException e) {
						logger.warn(
								"Unable to determine if configuration for {} is still present in the system repository",
								repositoryID);
					}
					catch (IOException e) {
						logger.warn("Unable to remove data dir for removed repository {} ", repositoryID);
					}
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

			repositories.clear();
		}
	}

	/**
	 * Clean up a removed repository. Note that the configuration for this
	 * repository is no longer present in the system repository.
	 * 
	 * @param repositoryID
	 *        the ID of the repository to clean up
	 * @throws IOException
	 */
	protected abstract void cleanUpRepository(String repositoryID)
		throws IOException;
}
