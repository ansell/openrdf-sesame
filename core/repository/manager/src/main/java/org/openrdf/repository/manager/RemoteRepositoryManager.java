/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.util.LiteralUtil;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
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

	/*------------------------*
	 * Static utility methods *
	 *------------------------*/

	/**
	 * Creates an initialized {@link RemoteRepositoryManager} with the specified
	 * server URL.
	 */
	public static RemoteRepositoryManager getInstance(String serverURL)
		throws RepositoryException
	{
		RemoteRepositoryManager manager = new RemoteRepositoryManager(serverURL);
		manager.initialize();
		return manager;
	}

	/**
	 * Creates an initialized {@link RemoteRepositoryManager} with the specified
	 * server URL and credentials.
	 */
	public static RemoteRepositoryManager getInstance(String serverURL, String username, String password)
		throws RepositoryException
	{
		RemoteRepositoryManager manager = new RemoteRepositoryManager(serverURL);
		manager.setUsernameAndPassword(username, password);
		manager.initialize();
		return manager;
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The URL of the remote server, e.g. http://localhost:8080/openrdf-sesame/
	 */
	private String serverURL;

	private String username;

	private String password;

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
	/**
	 * Set the username and password for authenication with the remote server.
	 * 
	 * @param username
	 *        the username
	 * @param password
	 *        the password
	 */
	public void setUsernameAndPassword(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	protected Repository createSystemRepository()
		throws RepositoryException
	{
		HTTPRepository systemRepository = new HTTPRepository(serverURL, SystemRepository.ID);
		systemRepository.setUsernameAndPassword(username, password);
		systemRepository.initialize();
		return systemRepository;
	}

	/**
	 * Gets the URL of the remote server, e.g.
	 * "http://localhost:8080/openrdf-sesame/".
	 */
	public String getServerURL() {
		return serverURL;
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
		HTTPRepository result = null;

		if (RepositoryConfigUtil.hasRepositoryConfig(getSystemRepository(), id)) {
			result = new HTTPRepository(serverURL, id);
			result.setUsernameAndPassword(username, password);
			result.initialize();
		}

		return result;
	}

	@Override
	public RepositoryInfo getRepositoryInfo(String id)
		throws RepositoryException
	{
		for (RepositoryInfo repInfo : getAllRepositoryInfos()) {
			if (repInfo.getId().equals(id)) {
				return repInfo;
			}
		}

		return null;
	}

	@Override
	public Collection<RepositoryInfo> getAllRepositoryInfos(boolean skipSystemRepo)
		throws RepositoryException
	{
		List<RepositoryInfo> result = new ArrayList<RepositoryInfo>();

		try {
			HTTPClient httpClient = new HTTPClient();
			httpClient.setServerURL(serverURL);
			httpClient.setUsernameAndPassword(username, password);

			TupleQueryResult responseFromServer = httpClient.getRepositoryList();
			while (responseFromServer.hasNext()) {
				BindingSet bindingSet = responseFromServer.next();
				RepositoryInfo repInfo = new RepositoryInfo();

				String id = LiteralUtil.getLabel(bindingSet.getValue("id"), null);

				if (skipSystemRepo && id.equals(SystemRepository.ID)) {
					continue;
				}

				Value uri = bindingSet.getValue("uri");
				String description = LiteralUtil.getLabel(bindingSet.getValue("title"), null);
				boolean readable = LiteralUtil.getBooleanValue(bindingSet.getValue("readable"), false);
				boolean writable = LiteralUtil.getBooleanValue(bindingSet.getValue("writable"), false);

				if (uri instanceof URI) {
					try {
						repInfo.setLocation(new URL(uri.toString()));
					}
					catch (MalformedURLException e) {
						logger.warn("Server reported malformed repository URL: {}", uri);
					}
				}

				repInfo.setId(id);
				repInfo.setDescription(description);
				repInfo.setReadable(readable);
				repInfo.setWritable(writable);

				result.add(repInfo);
			}
		}
		catch (IOException ioe) {
			logger.warn("Unable to retrieve list of repositories", ioe);
			throw new RepositoryException(ioe);
		}
		catch (QueryEvaluationException qee) {
			logger.warn("Unable to retrieve list of repositories", qee);
			throw new RepositoryException(qee);
		}
		catch (UnauthorizedException ue) {
			logger.warn("Not authorized to retrieve list of repositories", ue);
			throw new RepositoryException(ue);
		}
		catch (RepositoryException re) {
			logger.warn("Unable to retrieve list of repositories", re);
			throw re;
		}

		return result;
	}

	@Override
	protected void cleanUpRepository(String repositoryID)
		throws IOException
	{
		// do nothing
	}
}
