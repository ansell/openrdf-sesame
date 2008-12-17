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
import org.openrdf.http.client.helpers.FutureGraphQueryResult;
import org.openrdf.http.protocol.exceptions.NoCompatibleMediaType;
import org.openrdf.query.Binding;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.result.GraphResult;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class GraphQueryClient extends QueryClient {

	public GraphQueryClient(HTTPConnectionPool query) {
		super(query);
	}

	public GraphResult get(final Dataset dataset, final boolean includeInferred,
			final Binding... bindings)
		throws StoreException
	{
		Callable<GraphResult> task = new Callable<GraphResult>() {

			public GraphResult call()
				throws StoreException, MalformedQueryException
			{
				try {
					HTTPConnection method = get();
					method.acceptGraphQueryResult();
					execute(method, dataset, includeInferred, bindings);
					return method.getGraphQueryResult();
				}
				catch (NoCompatibleMediaType e) {
					throw new UnsupportedRDFormatException(e);
				}
				catch (IOException e) {
					throw new StoreException(e);
				}
				catch (RDFParseException e) {
					throw new StoreException(e);
				}
			}
		};
		return new FutureGraphQueryResult(submitTask(task));
	}

	public void get(Dataset dataset, boolean includeInferred, RDFHandler handler, Binding... bindings)
		throws RDFHandlerException, StoreException
	{
		HTTPConnection method = get();

		try {
			method.acceptRDF(false);
			execute(method, dataset, includeInferred, bindings);
			method.readRDF(handler);
		}
		catch (NoCompatibleMediaType e) {
			throw new UnsupportedRDFormatException(e);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		catch (RDFParseException e) {
			throw new StoreException(e);
		}
		finally {
			method.release();
		}
	}

}
