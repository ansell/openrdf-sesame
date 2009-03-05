/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.store.StoreConfigException;

/**
 * @author james
 */
public interface RepositoryConfigManager {

	/**
	 * Gets the URL of the server or directory.
	 * 
	 * @throws MalformedURLException
	 *         If the location cannot be represented as a URL.
	 */
	URL getLocation()
		throws MalformedURLException;

	Set<String> getIDs()
		throws StoreConfigException;

	Model getConfig(String repositoryID)
		throws StoreConfigException;

	/**
	 * Adds the configuration of a repository to the manager's system repository.
	 * The system repository may already contain a configuration for a repository
	 * with the same ID as specified by <tt>config</tt>, in which case all
	 * previous configuration data for that repository will be cleared before the
	 * new configuration is added.
	 * 
	 * @param config
	 *        The repository configuration that should be added to or updated in
	 *        the system repository.
	 * @throws StoreConfigException
	 *         If the manager doesn't know how to update a configuration due to
	 *         inconsistent configuration data in the system repository. For
	 *         example, this happens when there are multiple existing
	 *         configurations with the concerning ID.
	 */
	void addConfig(String id, Model config)
		throws StoreConfigException;

	/**
	 * Updates the configuration of a repository to the manager's system
	 * repository. The system repository may already contain a configuration for
	 * a repository with the same ID as specified by <tt>config</tt>, in which
	 * case all previous configuration data for that repository will be cleared
	 * before the new configuration is added.
	 * 
	 * @param config
	 *        The repository configuration that should be added to or updated in
	 *        the system repository.
	 * @throws StoreConfigException
	 *         If the manager doesn't know how to update a configuration due to
	 *         inconsistent configuration data in the system repository. For
	 *         example, this happens when there are multiple existing
	 *         configurations with the concerning ID.
	 */
	void updateConfig(String id, Model config)
		throws StoreConfigException;

	/**
	 * Removes the configuration for the specified repository from the manager's
	 * system repository if such a configuration is present. Returns
	 * <tt>true</tt> if the system repository actually contained the specified
	 * repository configuration.
	 * 
	 * @param repositoryID
	 *        The ID of the repository whose configuration needs to be removed.
	 * @throws StoreConfigException
	 *         If the manager doesn't know how to remove a configuration due to
	 *         inconsistent configuration data in the system repository. For
	 *         example, this happens when there are multiple existing
	 *         configurations with the concerning ID.
	 */
	void removeConfig(String repositoryID)
		throws StoreConfigException;

}