/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import org.openrdf.query.QueryResult;
import org.openrdf.store.StoreException;

/**
 * Wraps a boolean, needed because Spring MVC @ModuleAttribute does not support
 * primitives or their wrappers.
 * 
 * @author James Leigh
 */
public class BooleanQueryResult implements QueryResult<Boolean> {

	private Boolean result;

	public BooleanQueryResult(boolean result) {
		this.result = result;
	}

	public boolean getResult() {
		return result;
	}

	public void close()
		throws StoreException
	{
		// no-op
	}

	public boolean hasNext()
		throws StoreException
	{
		return result != null;
	}

	public Boolean next()
		throws StoreException
	{
		Boolean next = result;
		result = null;
		return next;
	}

	public void remove()
		throws StoreException
	{
		throw new UnsupportedOperationException();
	}

}
