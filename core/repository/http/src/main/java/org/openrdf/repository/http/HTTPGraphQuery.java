/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import org.openrdf.http.client.GraphQueryClient;
import org.openrdf.query.GraphQuery;
import org.openrdf.result.GraphResult;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.store.StoreException;

/**
 * GraphQuery implementation specific to the HTTP protocol. Methods in this
 * class may throw the specific StoreException subclasses UnautorizedException
 * and NotAllowedException, the semantics of which are defined by the HTTP
 * protocol.
 * 
 * @see org.openrdf.http.protocol.UnauthorizedException
 * @see org.openrdf.http.protocol.NotAllowedException
 * @author Arjohn Kampman
 * @author Herko ter Horst
 * @author James Leigh
 */
public class HTTPGraphQuery extends HTTPQuery implements GraphQuery {

	private GraphQueryClient client;

	public HTTPGraphQuery(String qry, GraphQueryClient client) {
		super(qry);
		this.client = client;
	}

	public GraphResult evaluate()
		throws StoreException
	{
		return client.get(dataset, includeInferred, getBindingsArray());
	}

	public <H extends RDFHandler> H evaluate(H handler)
		throws StoreException, RDFHandlerException
	{
		client.get(dataset, includeInferred, handler, getBindingsArray());
		return handler;
	}
}
