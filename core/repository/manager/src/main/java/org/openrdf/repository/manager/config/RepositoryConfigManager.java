/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
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
 * A manager for repository configuration.
 * 
 * @author james
 * @author Arjohn Kampman
 */
public interface RepositoryConfigManager {

	/**
	 * Gets the URL of the server or directory.
	 * 
	 * @throws MalformedURLException
	 *         If the location cannot be represented as a URL.
	 */
	public URL getLocation()
		throws MalformedURLException;

	/**
	 * Gets the identifiers of the available repository configurations.
	 * 
	 * @return The repository configuration identifiers.
	 * @throws StoreConfigException
	 *         If there was a problem getting the set of identifiers.
	 */
	public Set<String> getIDs()
		throws StoreConfigException;

	/**
	 * Gets the configuration for the specified repository.
	 * 
	 * @param repositoryID
	 *        The identifier of the repository to get the configuration for.
	 * @return The repository's configuration, or <tt>null</tt> if no such
	 *         configuration is available.
	 * @throws StoreConfigException
	 *         If there was a problem getting the repository configuration.
	 */
	public Model getConfig(String repositoryID)
		throws StoreConfigException;

	/**
	 * Adds the configuration of a repository. The new configuration will
	 * overwrite any existing configurations for the same ID.
	 * 
	 * @param repositoryID
	 *        The identifier for the repository.
	 * @param config
	 *        The repository configuration that should be added.
	 * @throws StoreConfigException
	 *         If there was a problem adding the configuration.
	 */
	public void addConfig(String repositoryID, Model config)
		throws StoreConfigException;

	/**
	 * Removes the configuration for the specified repository.
	 * 
	 * @param repositoryID
	 *        The ID of the repository whose configuration needs to be removed.
	 * @return <tt>true</tt> if such a configuration existed and was removed,
	 *         <tt>false</tt> otherwise.
	 * @throws StoreConfigException
	 *         If there was a problem removing the configuration.
	 */
	public boolean removeConfig(String repositoryID)
		throws StoreConfigException;
}