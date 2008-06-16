/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.RepositoryManager;

/**
 * 
 * @author Herko ter Horst
 */
public class TypeFilteringRepositoryManager extends RepositoryManager {

	private String type;

	private RepositoryManager delegate;

	/**
	 * 
	 */
	public TypeFilteringRepositoryManager(String type, RepositoryManager delegate) {
		this.type = type;
		this.delegate = delegate;
	}

	@Override
	public Collection<RepositoryInfo> getAllRepositoryInfos(boolean skipSystemRepo)
		throws RepositoryException
	{
		Collection<RepositoryInfo> result = delegate.getAllRepositoryInfos(skipSystemRepo);

		Iterator<RepositoryInfo> infoIt = result.iterator();
		while (infoIt.hasNext()) {
			try {
				if (!isCorrectType(infoIt.next().getId())) {
					infoIt.remove();
				}
			}
			catch (RepositoryConfigException e) {
				throw new RepositoryException(e);
			}
		}

		return result;
	}

	@Override
	public RepositoryInfo getRepositoryInfo(String id)
		throws RepositoryException
	{
		RepositoryInfo result = null;

		try {
			if (isCorrectType(id)) {
				result = delegate.getRepositoryInfo(id);
			}
		}
		catch (RepositoryConfigException e) {
			throw new RepositoryException(e);
		}
		return result;
	}

	/**
	 * @param config
	 * @throws RepositoryException
	 * @throws RepositoryConfigException
	 * @see org.openrdf.repository.manager.RepositoryManager#addRepositoryConfig(org.openrdf.repository.config.RepositoryConfig)
	 */
	public void addRepositoryConfig(RepositoryConfig config)
		throws RepositoryException, RepositoryConfigException
	{
		if (isCorrectType(config)) {
			delegate.addRepositoryConfig(config);
		}
		else {
			throw new UnsupportedOperationException("Only repositories of type " + type
					+ " can be added to this manager.");
		}
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	/**
	 * @return
	 * @throws RepositoryConfigException
	 * @throws RepositoryException
	 * @see org.openrdf.repository.manager.RepositoryManager#getAllRepositories()
	 */
	public Collection<Repository> getAllRepositories()
		throws RepositoryConfigException, RepositoryException
	{
		Set<String> idSet = getRepositoryIDs();

		ArrayList<Repository> result = new ArrayList<Repository>(idSet.size());

		for (String id : idSet) {
			if (isCorrectType(id)) {
				result.add(getRepository(id));
			}
		}

		return result;
	}

	/**
	 * @return
	 * @throws RepositoryException
	 * @see org.openrdf.repository.manager.RepositoryManager#getAllRepositoryInfos()
	 */
	public Collection<RepositoryInfo> getAllRepositoryInfos()
		throws RepositoryException
	{
		return getAllRepositoryInfos(false);
	}

	/**
	 * @return
	 * @throws RepositoryException
	 * @see org.openrdf.repository.manager.RepositoryManager#getAllUserRepositoryInfos()
	 */
	public Collection<RepositoryInfo> getAllUserRepositoryInfos()
		throws RepositoryException
	{
		return getAllRepositoryInfos(true);
	}

	/**
	 * @return
	 * @see org.openrdf.repository.manager.RepositoryManager#getInitializedRepositories()
	 */
	public Collection<Repository> getInitializedRepositories() {
		Set<String> idSet = getInitializedRepositoryIDs();

		ArrayList<Repository> result = new ArrayList<Repository>(idSet.size());

		for (String id : idSet) {
			try {
				result.add(getRepository(id));
			}
			catch (RepositoryConfigException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return result;
	}

	/**
	 * @return
	 * @see org.openrdf.repository.manager.RepositoryManager#getInitializedRepositoryIDs()
	 */
	public Set<String> getInitializedRepositoryIDs() {
		Set<String> result = delegate.getInitializedRepositoryIDs();

		Iterator<String> idIt = result.iterator();
		while (idIt.hasNext()) {
			try {
				if (isCorrectType(idIt.next())) {
					idIt.remove();
				}
			}
			catch (RepositoryConfigException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return result;
	}

	/**
	 * @param baseName
	 * @return
	 * @throws RepositoryException
	 * @throws RepositoryConfigException
	 * @see org.openrdf.repository.manager.RepositoryManager#getNewRepositoryID(java.lang.String)
	 */
	public String getNewRepositoryID(String baseName)
		throws RepositoryException, RepositoryConfigException
	{
		return delegate.getNewRepositoryID(baseName);
	}

	/**
	 * @param id
	 * @return
	 * @throws RepositoryConfigException
	 * @throws RepositoryException
	 * @see org.openrdf.repository.manager.RepositoryManager#getRepository(java.lang.String)
	 */
	public Repository getRepository(String id)
		throws RepositoryConfigException, RepositoryException
	{
		Repository result = null;

		if (isCorrectType(id)) {
			result = delegate.getRepository(id);
		}

		return result;
	}

	/**
	 * @param repositoryID
	 * @return
	 * @throws RepositoryConfigException
	 * @throws RepositoryException
	 * @see org.openrdf.repository.manager.RepositoryManager#getRepositoryConfig(java.lang.String)
	 */
	public RepositoryConfig getRepositoryConfig(String repositoryID)
		throws RepositoryConfigException, RepositoryException
	{
		RepositoryConfig result = delegate.getRepositoryConfig(repositoryID);

		if (!isCorrectType(result)) {
			result = null;
		}

		return result;
	}

	/**
	 * @return
	 * @throws RepositoryException
	 * @see org.openrdf.repository.manager.RepositoryManager#getRepositoryIDs()
	 */
	public Set<String> getRepositoryIDs()
		throws RepositoryException
	{
		Set<String> result = delegate.getRepositoryIDs();

		Iterator<String> idIt = result.iterator();
		while (idIt.hasNext()) {
			try {
				if (isCorrectType(idIt.next())) {
					idIt.remove();
				}
			}
			catch (RepositoryConfigException e) {
				throw new RepositoryException(e);
			}
		}

		return result;
	}

	/**
	 * @return
	 * @see org.openrdf.repository.manager.RepositoryManager#getSystemRepository()
	 */
	public Repository getSystemRepository() {
		return delegate.getSystemRepository();
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return delegate.hashCode();
	}

	/**
	 * @param repositoryID
	 * @return
	 * @throws RepositoryException
	 * @throws RepositoryConfigException
	 * @see org.openrdf.repository.manager.RepositoryManager#hasRepositoryConfig(java.lang.String)
	 */
	public boolean hasRepositoryConfig(String repositoryID)
		throws RepositoryException, RepositoryConfigException
	{
		boolean result = false;

		if (isCorrectType(repositoryID)) {
			result = delegate.hasRepositoryConfig(repositoryID);
		}

		return result;
	}

	/**
	 * @throws RepositoryException
	 * @see org.openrdf.repository.manager.RepositoryManager#initialize()
	 */
	public void initialize()
		throws RepositoryException
	{
		delegate.initialize();
	}

	/**
	 * 
	 * @see org.openrdf.repository.manager.RepositoryManager#refresh()
	 */
	public void refresh() {
		delegate.refresh();
	}

	/**
	 * @param repositoryID
	 * @return
	 * @throws RepositoryException
	 * @throws RepositoryConfigException
	 * @see org.openrdf.repository.manager.RepositoryManager#removeRepositoryConfig(java.lang.String)
	 */
	public boolean removeRepositoryConfig(String repositoryID)
		throws RepositoryException, RepositoryConfigException
	{
		boolean result = false;

		if (isCorrectType(repositoryID)) {
			result = delegate.removeRepositoryConfig(repositoryID);
		}

		return result;
	}

	/**
	 * 
	 * @see org.openrdf.repository.manager.RepositoryManager#shutDown()
	 */
	public void shutDown() {
		delegate.shutDown();
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return delegate.toString();
	}

	@Override
	protected void cleanUpRepository(String repositoryID)
		throws IOException
	{
		throw new UnsupportedOperationException("Repositories cannot be removed through this wrapper. This method should not have been called, the delegate should take care of it.");
	}

	@Override
	protected Repository createRepository(String id)
		throws RepositoryConfigException, RepositoryException
	{
		throw new UnsupportedOperationException("Repositories cannot be created through this wrapper. This method should not have been called, the delegate should take care of it.");
	}

	@Override
	protected Repository createSystemRepository()
		throws RepositoryException
	{
		throw new UnsupportedOperationException("The system repository cannot be created through this wrapper. This method should not have been called, the delegate should take care of it.");
	}	
	
	private boolean isCorrectType(String repositoryID)
		throws RepositoryConfigException, RepositoryException
	{
		return isCorrectType(delegate.getRepositoryConfig(repositoryID));
	}

	private boolean isCorrectType(RepositoryConfig repositoryConfig) {
		boolean result = false;

		if (repositoryConfig != null) {
			RepositoryImplConfig implConfig = repositoryConfig.getRepositoryImplConfig();
			result = implConfig.getType().equals(type);
			if(!result) {
				logger.warn("Unable to retrieve repository with ID {}: repository type {} did not match expected type {}", new Object[] {repositoryConfig.getID(), implConfig.getType(), type});
			}
		}

		return result;
	}
}
