/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.http.client.ConfigurationClient;
import org.openrdf.http.client.SesameClient;
import org.openrdf.model.Model;
import org.openrdf.store.StoreConfigException;

public class RemoteConfigManager implements RepositoryConfigManager {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final ConfigurationClient client;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public RemoteConfigManager(SesameClient client) {
		this(client.configurations());
	}

	public RemoteConfigManager(ConfigurationClient client) {
		this.client = client;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public URL getLocation()
		throws MalformedURLException
	{
		return new URL(client.getURL());
	}

	public void setUsernameAndPassword(String username, String password) {
		client.setUsernameAndPassword(username, password);
	}

	public Set<String> getIDs()
		throws StoreConfigException
	{
		return new HashSet<String>(client.list());
	}

	public Model getConfig(String repositoryID)
		throws StoreConfigException
	{
		return client.get(repositoryID);
	}

	public void addConfig(String id, Model config)
		throws StoreConfigException
	{
		client.put(id, config);
	}

	public boolean removeConfig(String repositoryID)
		throws StoreConfigException
	{
		return client.delete(repositoryID);
	}
}
