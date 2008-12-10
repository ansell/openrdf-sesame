/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.client.helpers.StoreClient;
import org.openrdf.store.StoreException;


/**
 *
 * @author James Leigh
 */
public class ConnectionsClient {

	private HTTPConnectionPool connections;
	private StoreClient client;

	public ConnectionsClient(HTTPConnectionPool connections) {
		this.connections = connections;
		this.client = new StoreClient(connections);
	}

	public ConnectionClient post()
		throws StoreException
	{
		String url = client.create();
		return new ConnectionClient(connections.location(url));
	}
}
