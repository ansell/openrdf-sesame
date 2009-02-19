/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.client.helpers.StoreConfigClient;
import org.openrdf.model.Model;
import org.openrdf.store.StoreConfigException;


/**
 *
 * @author James Leigh
 */
public class SchemaClient {
	private StoreConfigClient client;

	public SchemaClient(HTTPConnectionPool schemas) {
		this.client = new StoreConfigClient(schemas);
	}

	public Model get()
		throws StoreConfigException
	{
		return client.get(Model.class);
	}

}
