/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.helpers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.repository.Repository;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.manager.templates.ConfigTemplateManager;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

/**
 * TODO: make this a subclass of {@link RepositoryManager}.
 * 
 * @author Arjohn Kampman
 */
public class ServerRepositoryManager {

	private final RepositoryManager repoManager;

	private final Map<String, ServerRepository> repositories = new HashMap<String, ServerRepository>();

	public ServerRepositoryManager(RepositoryManager repoManager) {
		this.repoManager = repoManager;
	}

	public Set<String> getRepositoryIDs()
		throws StoreConfigException
	{
		return repoManager.getRepositoryIDs();
	}

	public synchronized ServerRepository getRepository(String id)
		throws StoreException, StoreConfigException
	{
		ServerRepository result = repositories.get(id);

		if (result == null) {
			Repository repository = repoManager.getRepository(id);

			if (repository != null) {
				result = new ServerRepository(repository);
				repositories.put(id, result);
			}
		}
		return result;
	}

	public synchronized void removeRepository(String id) {
		repositories.remove(id);
	}

	public ConfigTemplateManager getConfigTemplateManager()
		throws StoreConfigException
	{
		return repoManager.getConfigTemplateManager();
	}

	public Model getRepositoryConfig(String repositoryID)
		throws StoreConfigException
	{
		return repoManager.getRepositoryConfig(repositoryID);
	}

	public String addRepositoryConfig(String id, Model config)
		throws StoreConfigException, StoreException
	{
		return repoManager.addRepositoryConfig(id, config);
	}

	public boolean removeRepositoryConfig(String repositoryID)
		throws StoreException, StoreConfigException
	{
		return repoManager.removeRepositoryConfig(repositoryID);
	}

	public Collection<RepositoryInfo> getAllRepositoryInfos()
		throws StoreConfigException
	{
		return repoManager.getAllRepositoryInfos();
	}
}
