/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client.helpers;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class FutureTupleQueryResult implements TupleQueryResult {

	private Future<TupleQueryResult> delegate;

	public FutureTupleQueryResult(Future<TupleQueryResult> delegate) {
		this.delegate = delegate;
	}

	public List<String> getBindingNames() {
		try {
			return getDelegate().getBindingNames();
		}
		catch (StoreException e) {
			// FIXME getBindingNames should throw StoreException
			throw new AssertionError(e);
		}
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

	public void remove()
		throws StoreException
	{
		getDelegate().remove();
	}

	private TupleQueryResult getDelegate()
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
