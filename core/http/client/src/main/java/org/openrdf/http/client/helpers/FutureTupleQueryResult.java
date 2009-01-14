/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client.helpers;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.openrdf.query.BindingSet;
import org.openrdf.result.TupleResult;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class FutureTupleQueryResult implements TupleResult {

	private Future<TupleResult> delegate;

	public FutureTupleQueryResult(Future<TupleResult> delegate) {
		this.delegate = delegate;
	}

	public List<String> getBindingNames()
		throws StoreException
	{
		return getDelegate().getBindingNames();
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

	public BindingSet next()
		throws StoreException
	{
		return getDelegate().next();
	}

	public BindingSet getSingle()
		throws StoreException
	{
		return getDelegate().getSingle();
	}

	public <C extends Collection<? super BindingSet>> C addTo(C collection)
		throws StoreException
	{
		return getDelegate().addTo(collection);
	}

	public List<BindingSet> asList()
		throws StoreException
	{
		return getDelegate().asList();
	}

	public Set<BindingSet> asSet()
		throws StoreException
	{
		return getDelegate().asSet();
	}

	private TupleResult getDelegate()
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
