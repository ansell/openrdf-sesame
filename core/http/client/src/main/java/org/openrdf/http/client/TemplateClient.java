/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
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
 *
 * @author James Leigh
 */
public class TemplateClient {
	private StoreConfigClient client;

	public TemplateClient(HTTPConnectionPool templates) {
		this.client = new StoreConfigClient(templates);
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

}
