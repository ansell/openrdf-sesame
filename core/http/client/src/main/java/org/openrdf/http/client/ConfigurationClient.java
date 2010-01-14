/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import java.util.List;

import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.client.helpers.StoreConfigClient;
import org.openrdf.model.Model;
import org.openrdf.store.StoreConfigException;

/**
 * @author James Leigh
 */
public class ConfigurationClient {

	private final StoreConfigClient client;

	public ConfigurationClient(HTTPConnectionPool pool) {
		this.client = new StoreConfigClient(pool);
	}

	public String getURL() {
		return client.getURL();
	}

	public void setUsernameAndPassword(String username, String password) {
		client.setUsernameAndPassword(username, password);
	}

	public List<String> list()
		throws StoreConfigException
	{
		return client.list();
	}

	public Model get(String id)
		throws StoreConfigException
	{
		return client.get(id, Model.class);
	}

	public void put(String id, Model model)
		throws StoreConfigException
	{
		client.put(id, model);
	}

	public boolean delete(String id)
		throws StoreConfigException
	{
		return client.delete(id);
	}
}
