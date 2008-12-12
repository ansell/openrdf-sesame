/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import java.io.IOException;

import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.protocol.exceptions.NoCompatibleMediaType;
import org.openrdf.query.Binding;
import org.openrdf.query.Dataset;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class BooleanQueryClient extends QueryClient {

	public BooleanQueryClient(HTTPConnectionPool query) {
		super(query);
	}

	public boolean get(Dataset dataset, boolean includeInferred, Binding... bindings)
		throws StoreException
	{
		HTTPConnection method = get();

		try {
			method.acceptBoolean();
			execute(method, dataset, includeInferred, bindings);
			return method.readBoolean();
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
