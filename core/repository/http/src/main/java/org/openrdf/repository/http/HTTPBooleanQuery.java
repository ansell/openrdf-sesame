/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import org.openrdf.http.client.BooleanQueryClient;
import org.openrdf.query.BooleanQuery;
import org.openrdf.store.StoreException;

/**
 * TupleQuery specific to the HTTP protocol. Methods in this class may throw the
 * specific StoreException subclasses UnautorizedException and
 * NotAllowedException, the semantics of which are defined by the HTTP protocol.
 * 
 * @see org.openrdf.http.protocol.UnauthorizedException
 * @see org.openrdf.http.protocol.NotAllowedException
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class HTTPBooleanQuery extends HTTPQuery implements BooleanQuery {

	private BooleanQueryClient client;

	public HTTPBooleanQuery(String qry, BooleanQueryClient client) {
		super(qry);
		this.client = client;
	}

	public boolean evaluate()
		throws StoreException
	{
		return client.get(dataset, includeInferred, getBindingsArray());
	}
}
