/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.repository.manager.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.client.HttpClient;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.RepositoryInfo;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.manager.SystemRepository;

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

	/**
	 * @see org.eclipse.rdf4j.repository.manager.RepositoryManager#getHttpClient()
	 */
	public HttpClient getHttpClient() {
		return delegate.getHttpClient();
	}

	/**
	 * @param httpClient
	 * @see org.eclipse.rdf4j.repository.manager.RepositoryManager#setHttpClient(org.apache.http.client.HttpClient)
	 */
	public void setHttpClient(HttpClient httpClient) {
		delegate.setHttpClient(httpClient);
	}

	@Override
	public void initialize()
		throws RepositoryException
	{
		delegate.initialize();
	}

	@Override
	public URL getLocation()
		throws MalformedURLException
	{
		return delegate.getLocation();
	}

	@Override
	protected Repository createSystemRepository()
		throws RepositoryException
	{
		throw new UnsupportedOperationException(
				"The system repository cannot be created through this wrapper. This method should not have been called, the delegate should take care of it.");
	}

	@Override
	public Repository getSystemRepository() {
		return delegate.getSystemRepository();
	}

	@Override
	public String getNewRepositoryID(String baseName)
		throws RepositoryException, RepositoryConfigException
	{
		return delegate.getNewRepositoryID(baseName);
	}

	@Override
	public Set<String> getRepositoryIDs()
		throws RepositoryException
	{
		Set<String> result = new LinkedHashSet<String>();

		for (String id : delegate.getRepositoryIDs()) {
			try {
				if (isCorrectType(id)) {
					result.add(id);
				}
			}
			catch (RepositoryConfigException e) {
				throw new RepositoryException(e);
			}
		}

		return result;
	}

	@Override
	public boolean hasRepositoryConfig(String repositoryID)
		throws RepositoryException, RepositoryConfigException
	{
		boolean result = false;

		if (isCorrectType(repositoryID)) {
			result = delegate.hasRepositoryConfig(repositoryID);
		}

		return result;
	}

	@Override
	public RepositoryConfig getRepositoryConfig(String repositoryID)
		throws RepositoryConfigException, RepositoryException
	{
		RepositoryConfig result = delegate.getRepositoryConfig(repositoryID);

		if (result != null) {
			if (!isCorrectType(result)) {
				logger.debug(
						"Surpressing retrieval of repository {}: repository type {} did not match expected type {}",
						new Object[] { result.getID(), result.getRepositoryImplConfig().getType(), type });

				result = null;
			}
		}

		return result;
	}

	@Override
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

	@Override
	@Deprecated
	public boolean removeRepositoryConfig(String repositoryID)
		throws RepositoryException, RepositoryConfigException
	{
		boolean result = false;

		if (isCorrectType(repositoryID)) {
			result = delegate.removeRepositoryConfig(repositoryID);
		}

		return result;
	}

	@Override
	public Repository getRepository(String id)
		throws RepositoryConfigException, RepositoryException
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
			catch (RepositoryConfigException e) {
				logger.error("Failed to verify repository type", e);
			}
			catch (RepositoryException e) {
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
			catch (RepositoryConfigException e) {
				logger.error("Failed to verify repository type", e);
			}
			catch (RepositoryException e) {
				logger.error("Failed to verify repository type", e);
			}
		}

		return result;
	}

	@Override
	protected Repository createRepository(String id)
		throws RepositoryConfigException, RepositoryException
	{
		throw new UnsupportedOperationException(
				"Repositories cannot be created through this wrapper. This method should not have been called, the delegate should take care of it.");
	}

	@Override
	public Collection<RepositoryInfo> getAllRepositoryInfos(boolean skipSystemRepo)
		throws RepositoryException
	{
		List<RepositoryInfo> result = new ArrayList<RepositoryInfo>();

		for (RepositoryInfo repInfo : delegate.getAllRepositoryInfos(skipSystemRepo)) {
			try {
				if (isCorrectType(repInfo.getId())) {
					result.add(repInfo);
				}
			}
			catch (RepositoryConfigException e) {
				throw new RepositoryException(e.getMessage(), e);
			}
		}

		return result;
	}

	@Override
	public RepositoryInfo getRepositoryInfo(String id)
		throws RepositoryException
	{
		try {
			if (isCorrectType(id)) {
				return delegate.getRepositoryInfo(id);
			}

			return null;
		}
		catch (RepositoryConfigException e) {
			throw new RepositoryException(e.getMessage(), e);
		}
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
		throws RepositoryConfigException, RepositoryException
	{
		// first, check for SystemRepository, because we can't get a repository
		// config object for it
		boolean result = !SystemRepository.ID.equals(repositoryID);
		if (result) {
			result = isCorrectType(delegate.getRepositoryConfig(repositoryID));
		}
		return result;
	}

	protected boolean isCorrectType(RepositoryConfig repositoryConfig) {
		boolean result = false;

		if (repositoryConfig != null) {
			result = repositoryConfig.getRepositoryImplConfig().getType().equals(type);
		}

		return result;
	}
}
