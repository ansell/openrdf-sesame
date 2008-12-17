/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import org.openrdf.http.client.BooleanQueryClient;
import org.openrdf.query.BooleanQuery;
import org.openrdf.result.BooleanResult;
import org.openrdf.result.impl.BooleanResultImpl;
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

	public BooleanResult evaluate()
		throws StoreException
	{
		return new BooleanResultImpl(ask());
	}

	public boolean ask()
		throws StoreException
	{
		return client.get(dataset, includeInferred, getBindingsArray());
	}
}
