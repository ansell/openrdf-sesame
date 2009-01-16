/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client.helpers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.result.GraphResult;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class FutureGraphQueryResult implements GraphResult {

	private Future<GraphResult> delegate;

	public FutureGraphQueryResult(Future<GraphResult> delegate) {
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

	public Statement singleResult()
		throws StoreException
	{
		return getDelegate().singleResult();
	}

	public <C extends Collection<? super Statement>> C addTo(C collection)
		throws StoreException
	{
		return getDelegate().addTo(collection);
	}

	public List<Statement> asList()
		throws StoreException
	{
		return getDelegate().asList();
	}

	public Set<Statement> asSet()
		throws StoreException
	{
		return getDelegate().asSet();
	}

	public Model asModel()
		throws StoreException
	{
		return getDelegate().asModel();
	}

	private GraphResult getDelegate()
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
				throw (StoreException)e.getCause();
			throw new StoreException(e);
		}
	}

}
