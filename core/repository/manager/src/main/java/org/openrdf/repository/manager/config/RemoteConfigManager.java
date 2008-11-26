/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager.config;

import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORYID;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.http.client.ConfigurationClient;
import org.openrdf.http.client.SesameClient;
import org.openrdf.model.Model;
import org.openrdf.model.Value;
import org.openrdf.store.StoreConfigException;

public class RemoteConfigManager implements RepositoryConfigManager {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ConfigurationClient client;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public RemoteConfigManager(String serverURL) {
		this(new SesameClient(serverURL));
	}

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

	public void addConfig(Model config)
		throws StoreConfigException
	{
		String id = getId(config);
		client.put(id, config);
	}

	public void updateConfig(Model config)
		throws StoreConfigException
	{
		String id = getId(config);
		client.put(id, config);
	}

	public void removeConfig(String repositoryID)
		throws StoreConfigException
	{
		client.delete(repositoryID);
	}

	private String getId(Model config)
		throws StoreConfigException
	{
		for (Value value : config.filter(null, REPOSITORYID, null).objects()) {
			return value.stringValue();
		}
		throw new StoreConfigException("No repository id present");
	}
}
