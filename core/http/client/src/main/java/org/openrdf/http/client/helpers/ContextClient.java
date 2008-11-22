/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client.helpers;

import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.TupleQueryResultBuilder;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class ContextClient {

	private HTTPConnectionPool contexts;

	public ContextClient(HTTPConnectionPool contexts) {
		this.contexts = contexts;
	}

	/*-------------*
	 * Context IDs *
	 *-------------*/

	public TupleQueryResult get()
		throws StoreException
	{
		try {
			TupleQueryResultBuilder builder = new TupleQueryResultBuilder();
			get(builder);
			return builder.getQueryResult();
		}
		catch (TupleQueryResultHandlerException e) {
			// Found a bug in TupleQueryResultBuilder?
			throw new AssertionError(e);
		}
	}

	public void get(TupleQueryResultHandler handler)
		throws TupleQueryResultHandlerException, StoreException
	{
		HTTPConnection method = contexts.get();

		try {
			method.acceptTuple();
			method.execute();
			method.readTuple(handler);
		}
		finally {
			method.release();
		}
	}

}
