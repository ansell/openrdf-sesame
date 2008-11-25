/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager.util;

import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORY;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.manager.SystemRepository;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

/**
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class TypeFilteringRepositoryManager extends RepositoryManager {

	private final String type;

	private final RepositoryManager delegate;

	public TypeFilteringRepositoryManager(String type, RepositoryManager delegate) {
		assert type != null : "type must not be null";
		assert delegate != null : "delegate must not be null";

		this.type = type;
		this.delegate = delegate;
	}

	@Override
	public void initialize()
		throws StoreConfigException
	{
		delegate.initialize();
	}

	@Override
	public URL getLocation() throws MalformedURLException {
		return delegate.getLocation();
	}

	@Override
	protected Repository createSystemRepository()
		throws StoreException
	{
		throw new UnsupportedOperationException(
				"The system repository cannot be created through this wrapper. This method should not have been called, the delegate should take care of it.");
	}

	@Override
	public Repository getSystemRepository() throws StoreException, StoreConfigException {
		return delegate.getSystemRepository();
	}

	@Override
	public String getNewRepositoryID(String baseName)
		throws StoreConfigException
	{
		return delegate.getNewRepositoryID(baseName);
	}

	@Override
	public Set<String> getRepositoryIDs()
		throws StoreConfigException
	{
		Set<String> result = new LinkedHashSet<String>();

		for (String id : delegate.getRepositoryIDs()) {
			if (isCorrectType(id)) {
				result.add(id);
			}
		}

		return result;
	}

	@Override
	public boolean hasRepositoryConfig(String repositoryID)
		throws StoreConfigException
	{
		boolean result = false;

		if (isCorrectType(repositoryID)) {
			result = delegate.hasRepositoryConfig(repositoryID);
		}

		return result;
	}

	@Override
	public Model getRepositoryConfig(String repositoryID)
		throws StoreConfigException
	{
		Model result = delegate.getRepositoryConfig(repositoryID);

		if (result != null) {
			if (!isCorrectType(result)) {
				RepositoryConfig config = parse(result);
				logger.debug(
						"Surpressing retrieval of repository {}: repository type {} did not match expected type {}",
						new Object[] { config.getID(), config.getRepositoryImplConfig().getType(), type });

				result = null;
			}
		}

		return result;
	}

	@Override
	public String addRepositoryConfig(Model config)
		throws StoreConfigException, StoreException
	{
		if (isCorrectType(config)) {
			return delegate.addRepositoryConfig(config);
		}
		else {
			throw new UnsupportedOperationException("Only repositories of type " + type
					+ " can be added to this manager.");
		}
	}

	@Override
	public boolean removeRepositoryConfig(String repositoryID)
		throws StoreConfigException, StoreException
	{
		boolean result = false;

		if (isCorrectType(repositoryID)) {
			result = delegate.removeRepositoryConfig(repositoryID);
		}

		return result;
	}

	@Override
	public Repository getRepository(String id)
		throws StoreConfigException, StoreException
	{
		Repository result = null;

		if (isCorrectType(id)) {
			result = delegate.getRepository(id);
		}

		return result;
	}

	@Override
	public Set<String> getInitializedRepositoryIDs() {
		Set<String> result = new LinkedHashSet<String>();

		for (String id : delegate.getInitializedRepositoryIDs()) {
			try {
				if (isCorrectType(id)) {
					result.add(id);
				}
			}
			catch (StoreConfigException e) {
				logger.error("Failed to verify repository type", e);
			}
		}

		return result;
	}

	@Override
	public Collection<Repository> getInitializedRepositories() {
		List<Repository> result = new ArrayList<Repository>();

		for (String id : getInitializedRepositoryIDs()) {
			try {
				Repository repository = getRepository(id);

				if (repository != null) {
					result.add(repository);
				}
			}
			catch (StoreConfigException e) {
				logger.error("Failed to verify repository type", e);
			}
			catch (StoreException e) {
				logger.error("Failed to verify repository type", e);
			}
		}

		return result;
	}

	@Override
	protected Repository createRepository(String id)
		throws StoreConfigException, StoreException
	{
		throw new UnsupportedOperationException(
				"Repositories cannot be created through this wrapper. This method should not have been called, the delegate should take care of it.");
	}

	@Override
	public Collection<RepositoryInfo> getAllRepositoryInfos(boolean skipSystemRepo)
		throws StoreConfigException
	{
		List<RepositoryInfo> result = new ArrayList<RepositoryInfo>();

		for (RepositoryInfo repInfo : delegate.getAllRepositoryInfos(skipSystemRepo)) {
			if (isCorrectType(repInfo.getId())) {
				result.add(repInfo);
			}
		}

		return result;
	}

	@Override
	public RepositoryInfo getRepositoryInfo(String id)
		throws StoreConfigException
	{
		if (isCorrectType(id)) {
			return delegate.getRepositoryInfo(id);
		}

		return null;
	}

	@Override
	public void refresh() {
		delegate.refresh();
	}

	@Override
	public void shutDown() {
		delegate.shutDown();
	}

	@Override
	protected void cleanUpRepository(String repositoryID)
		throws IOException
	{
		throw new UnsupportedOperationException(
				"Repositories cannot be removed through this wrapper. This method should not have been called, the delegate should take care of it.");
	}

	protected boolean isCorrectType(String repositoryID)
		throws StoreConfigException
	{
		// first, check for SystemRepository, because we can't get a repository
		// config object for it
		boolean result = !SystemRepository.ID.equals(repositoryID);
		if (result) {
			result = isCorrectType(delegate.getRepositoryConfig(repositoryID));
		}
		return result;
	}

	protected boolean isCorrectType(Model repositoryConfig) throws StoreConfigException {
		boolean result = false;

		if (repositoryConfig != null) {
			result = parse(repositoryConfig).getRepositoryImplConfig().getType().equals(type);
		}

		return result;
	}

	private RepositoryConfig parse(Model config)
		throws StoreConfigException
	{
		Resource repositoryNode = config.filter(null, RDF.TYPE, REPOSITORY).subjects().iterator().next();
		RepositoryConfig repConfig = RepositoryConfig.create(config, repositoryNode);
		repConfig.validate();
		return repConfig;
	}
}
