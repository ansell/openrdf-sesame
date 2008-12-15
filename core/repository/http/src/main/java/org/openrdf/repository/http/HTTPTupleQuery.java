/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import org.openrdf.http.client.TupleQueryClient;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.results.TupleResult;
import org.openrdf.store.StoreException;

/**
 * TupleQuery specific to the HTTP protocol. Methods in this class may throw the
 * specific StoreException subclasses UnautorizedException and
 * NotAllowedException, the semantics of which are defined by the HTTP protocol.
 * 
 * @see org.openrdf.http.protocol.UnauthorizedException
 * @see org.openrdf.http.protocol.NotAllowedException
 * @author Arjohn Kampman
 * @author Herko ter Horst
 * @author James Leigh
 */
public class HTTPTupleQuery extends HTTPQuery implements TupleQuery {

	private TupleQueryClient client;

	public HTTPTupleQuery(String qry, TupleQueryClient client) {
		super(qry);
		this.client = client;
	}

	public TupleResult evaluate()
		throws StoreException
	{
		return client.get(dataset, includeInferred, getBindingsArray());
	}

	public void evaluate(TupleQueryResultHandler handler)
		throws StoreException, TupleQueryResultHandlerException
	{
		client.get(dataset, includeInferred, handler, getBindingsArray());
	}
}
