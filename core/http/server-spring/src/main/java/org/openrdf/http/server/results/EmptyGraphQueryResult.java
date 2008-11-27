/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.results;

import java.util.Collections;
import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.store.StoreException;



/**
 *
 * @author James Leigh
 */
public class EmptyGraphQueryResult implements GraphQueryResult {

	public static EmptyGraphQueryResult EMPTY = new EmptyGraphQueryResult();

	public Map<String, String> getNamespaces() {
		return Collections.emptyMap();
	}

	public void close()
		throws StoreException
	{
		// no-op
	}

	public boolean hasNext()
		throws StoreException
	{
		return false;
	}

	public Statement next()
		throws StoreException
	{
		return null;
	}

	public void remove()
		throws StoreException
	{
		throw new IllegalStateException();
	}

}
