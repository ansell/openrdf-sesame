/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.openrdf.http.client.connections.HTTPRequest;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.client.helpers.FutureGraphQueryResult;
import org.openrdf.http.protocol.exceptions.NoCompatibleMediaType;
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

	public GraphResult get()
		throws StoreException
	{
		Callable<GraphResult> task = new Callable<GraphResult>() {

			public GraphResult call()
				throws Exception
			{
				try {
					HTTPRequest request = createRequest();
					request.acceptGraphQueryResult();
					execute(request);
					return request.getGraphQueryResult();
				}
				catch (NoCompatibleMediaType e) {
					throw new UnsupportedRDFormatException(e);
				}
			}
		};
		return new FutureGraphQueryResult(submitTask(task));
	}

	public void get(RDFHandler handler)
		throws RDFHandlerException, StoreException
	{
		HTTPRequest request = createRequest();

		try {
			request.acceptRDF(false);
			execute(request);
			request.readRDF(handler);
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
			request.release();
		}
	}
}
