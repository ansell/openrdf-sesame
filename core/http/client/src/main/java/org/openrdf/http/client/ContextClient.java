/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2002-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.client.helpers.StoreClient;
import org.openrdf.result.TupleResult;
import org.openrdf.store.StoreException;

/**
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class ContextClient {

	private final StoreClient client;

	public ContextClient(HTTPConnectionPool contexts) {
		this.client = new StoreClient(contexts);
	}

	/**
	 * Gets the list of context IDs.
	 */
	public TupleResult list()
		throws StoreException
	{
		return client.list();
	}
}
