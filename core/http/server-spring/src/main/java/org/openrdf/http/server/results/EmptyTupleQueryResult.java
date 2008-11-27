/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.results;

import java.util.Collections;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.store.StoreException;


/**
 *
 * @author James Leigh
 */
public class EmptyTupleQueryResult implements TupleQueryResult {

	public static EmptyTupleQueryResult EMPTY = new EmptyTupleQueryResult();

	public List<String> getBindingNames() {
		return Collections.emptyList();
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

	public BindingSet next()
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
