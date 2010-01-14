/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import java.io.IOException;

import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.protocol.exceptions.NoCompatibleMediaType;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class BooleanQueryClient extends QueryClient {

	public BooleanQueryClient(HTTPConnectionPool pool) {
		super(pool);
	}

	public boolean get()
		throws StoreException
	{
		HTTPConnection con = createConnection();

		try {
			con.acceptBoolean();
			execute(con);
			return con.readBoolean();
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
			con.release();
		}
	}
}
