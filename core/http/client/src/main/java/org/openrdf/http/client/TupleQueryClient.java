/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.client.helpers.FutureTupleQueryResult;
import org.openrdf.http.protocol.exceptions.NoCompatibleMediaType;
import org.openrdf.query.Binding;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class TupleQueryClient extends QueryClient {

	public TupleQueryClient(HTTPConnectionPool query) {
		super(query);
	}

	public TupleQueryResult get(final Dataset dataset, final boolean includeInferred,
			final Binding... bindings)
		throws StoreException
	{
		Callable<TupleQueryResult> task = new Callable<TupleQueryResult>() {

			public TupleQueryResult call()
				throws StoreException, MalformedQueryException
			{
				try {
					HTTPConnection method = get();
					method.acceptTupleQueryResult();
					execute(method, dataset, includeInferred, bindings);
					return method.getTupleQueryResult();
				}
				catch (NoCompatibleMediaType e) {
					throw new UnsupportedRDFormatException(e);
				}
				catch (IOException e) {
					throw new StoreException(e);
				}
				catch (QueryResultParseException e) {
					throw new StoreException(e);
				}
			}
		};
		return new FutureTupleQueryResult(submitTask(task));
	}

	public void get(Dataset dataset, boolean includeInferred, TupleQueryResultHandler handler,
			Binding... bindings)
		throws TupleQueryResultHandlerException, StoreException
	{
		HTTPConnection method = get();

		try {
			method.acceptRDF(false);
			execute(method, dataset, includeInferred, bindings);
			method.readTupleQueryResult(handler);
		}
		catch (NoCompatibleMediaType e) {
			throw new UnsupportedRDFormatException(e);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		catch (QueryResultParseException e) {
			throw new StoreException(e);
		}
		finally {
			method.release();
		}
	}

}
