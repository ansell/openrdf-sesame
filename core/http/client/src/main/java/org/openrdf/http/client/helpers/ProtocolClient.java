/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2002-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client.helpers;

import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.store.StoreException;

/**
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class ProtocolClient {

	private HTTPConnectionPool size;

	public ProtocolClient(HTTPConnectionPool size) {
		this.size = size;
	}

	public String get()
		throws StoreException
	{
		HTTPConnection method = size.get();
		try {
			method.acceptString();
			method.execute();
			return method.readString();
		}
		finally {
			method.release();
		}
	}

}
