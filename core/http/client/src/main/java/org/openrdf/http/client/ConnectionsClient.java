/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.client.helpers.StoreClient;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class ConnectionsClient {

	private final HTTPConnectionPool pool;

	private final StoreClient client;

	public ConnectionsClient(HTTPConnectionPool pool) {
		this.pool = pool;
		this.client = new StoreClient(pool);
	}

	public ConnectionClient post()
		throws StoreException
	{
		String url = client.create();
		return new ConnectionClient(pool.location(url));
	}
}
