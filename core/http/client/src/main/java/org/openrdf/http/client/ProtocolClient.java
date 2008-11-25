/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2002-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.client.helpers.StoreClient;
import org.openrdf.store.StoreException;

/**
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class ProtocolClient {
	private StoreClient client;

	public ProtocolClient(HTTPConnectionPool size) {
		this.client = new StoreClient(size);
	}

	public String get()
		throws StoreException
	{
		return client.get(String.class);
	}

}
