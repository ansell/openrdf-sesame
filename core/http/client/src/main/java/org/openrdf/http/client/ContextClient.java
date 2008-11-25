/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2002-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.client.helpers.StoreClient;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.store.StoreException;

/**
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class ContextClient {

	private StoreClient client;

	public ContextClient(HTTPConnectionPool contexts) {
		this.client = new StoreClient(contexts);
	}

	/*-------------*
	 * Context IDs *
	 *-------------*/

	public TupleQueryResult list()
		throws StoreException
	{
		return client.list();
	}

}
