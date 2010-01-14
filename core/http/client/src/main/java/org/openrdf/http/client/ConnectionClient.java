/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.client.helpers.StoreClient;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class ConnectionClient extends RepositoryClient {

	private final HTTPConnectionPool pool;

	public ConnectionClient(HTTPConnectionPool pool) {
		super(pool);
		this.pool = pool;
	}

	public BNodeClient bnodes() {
		return new BNodeClient(pool.slash(Protocol.BNODES));
	}

	public void begin()
		throws StoreException
	{
		new StoreClient(pool.slash(Protocol.BEGIN)).post();
	}

	public void ping()
		throws StoreException
	{
		new StoreClient(pool.slash(Protocol.PING)).post();
	}

	public void commit()
		throws StoreException
	{
		new StoreClient(pool.slash(Protocol.COMMIT)).post();
	}

	public void rollback()
		throws StoreException
	{
		new StoreClient(pool.slash(Protocol.ROLLBACK)).post();
	}

	public void close()
		throws StoreException
	{
		new StoreClient(pool).delete();
	}

	public QueriesClient queries() {
		return new QueriesClient(pool.slash(Protocol.QUERIES));
	}
}
