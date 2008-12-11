/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client.helpers;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.openrdf.model.Statement;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.store.StoreException;


/**
 *
 * @author James Leigh
 */
public class FutureGraphQueryResult implements GraphQueryResult {

	private Future<GraphQueryResult> delegate;

	public FutureGraphQueryResult(Future<GraphQueryResult> delegate) {
		this.delegate = delegate;
	}

	public Map<String, String> getNamespaces()
		throws StoreException
	{
		return getDelegate().getNamespaces();
	}

	public void close()
		throws StoreException
	{
		getDelegate().close();
	}

	public boolean hasNext()
		throws StoreException
	{
		return getDelegate().hasNext();
	}

	public Statement next()
		throws StoreException
	{
		return getDelegate().next();
	}

	public void remove()
		throws StoreException
	{
		getDelegate().remove();
	}

	private GraphQueryResult getDelegate()
		throws StoreException
	{
		try {
			return delegate.get();
		}
		catch (InterruptedException e) {
			throw new StoreException(e);
		}
		catch (ExecutionException e) {
			if (e.getCause() instanceof StoreException)
				throw (StoreException) e.getCause();
			throw new StoreException(e);
		}
	}

}
