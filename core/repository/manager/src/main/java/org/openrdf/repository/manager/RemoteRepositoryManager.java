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

import org.openrdf.http.client.SesameClient;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.util.LiteralUtil;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.Repository;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.manager.config.RemoteConfigManager;
import org.openrdf.repository.manager.templates.RemoteTemplateManager;
import org.openrdf.result.TupleResult;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

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
		throws StoreConfigException
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
		throws StoreConfigException
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
	 * Creates a new RepositoryManager that operates on the specified base
	 * directory.
	 * 
	 * @param serverURL
	 *        The URL of the server.
	 */
	public RemoteRepositoryManager(String serverURL) {
		super();
		this.serverURL = serverURL;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Initializes the repository manager.
	 */
	public void initialize() {
		SesameClient client = new SesameClient(serverURL);
		client.setUsernameAndPassword(username, password);
		setConfigTemplateManager(new RemoteTemplateManager(client));
		setRepositoryConfigManager(new RemoteConfigManager(client));
	}

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

	/**
	 * Gets the URL of the remote server, e.g.
	 * "http://localhost:8080/openrdf-sesame/".
	 * 
	 * @throws MalformedURLException
	 *         If serverURL cannot be parsed
	 */
	public URL getLocation()
		throws MalformedURLException
	{
		return new URL(serverURL);
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
	 * @throws StoreConfigException
	 *         If no repository could be created due to invalid or incomplete
	 *         configuration data.
	 */
	@Override
	protected Repository createRepository(String id)
		throws StoreConfigException, StoreException
	{
		HTTPRepository result = null;

		if (hasRepositoryConfig(id)) {
			result = new HTTPRepository(serverURL, id);
			result.setUsernameAndPassword(username, password);
			result.initialize();
		}

		return result;
	}

	@Override
	public RepositoryInfo getRepositoryInfo(String id)
		throws StoreConfigException
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
		throws StoreConfigException
	{
		List<RepositoryInfo> result = new ArrayList<RepositoryInfo>();

		try {
			SesameClient client = new SesameClient(serverURL);
			client.setUsernameAndPassword(username, password);

			TupleResult responseFromServer = client.repositories().list();
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
		catch (UnauthorizedException ue) {
			logger.warn("Not authorized to retrieve list of repositories", ue);
			throw new StoreConfigException(ue);
		}
		catch (StoreException re) {
			logger.warn("Unable to retrieve list of repositories", re);
			throw new StoreConfigException(re);
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
