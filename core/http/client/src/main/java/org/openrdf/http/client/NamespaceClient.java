/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2002-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.TupleQueryResultBuilder;
import org.openrdf.store.StoreException;

/**
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class NamespaceClient {

	private HTTPConnectionPool namespaces;

	public NamespaceClient(HTTPConnectionPool namespaces) {
		this.namespaces = namespaces;
	}

	/*---------------------------*
	 * Get/add/remove namespaces *
	 *---------------------------*/

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
			throw new RuntimeException(e);
		}
	}

	public void get(TupleQueryResultHandler handler)
		throws TupleQueryResultHandlerException, StoreException
	{
		HTTPConnection method = namespaces.get();

		try {
			method.acceptTuple();
			method.executeQuery();
			method.readTuple(handler);
		}
		catch (MalformedQueryException e) {
			throw new StoreException(e.getMessage(), e);
		}
		finally {
			method.release();
		}
	}

	public String get(String prefix)
		throws StoreException
	{
		HTTPConnection method = namespaces.slash(prefix).get();

		try {
			method.acceptString();
			method.execute();
			return method.readString();
		}
		finally {
			method.release();
		}
	}

	public void put(String prefix, String name)
		throws StoreException
	{
		HTTPConnection method = namespaces.slash(prefix).put();
		method.sendString(name);

		try {
			method.execute();
		}
		finally {
			method.release();
		}
	}

	public void delete(String prefix)
		throws StoreException
	{
		HTTPConnection method = namespaces.slash(prefix).delete();

		try {
			method.execute();
		}
		finally {
			method.release();
		}
	}

	public void delete()
		throws StoreException
	{
		HTTPConnection method = namespaces.delete();

		try {
			method.execute();
		}
		finally {
			method.release();
		}
	}

}
