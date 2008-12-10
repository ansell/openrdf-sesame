/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.evaluation;

import org.openrdf.query.Cursor;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.store.StoreException;

/**
 * Wrap a RepositoryResult as a Cursor.
 * 
 * @author James Leigh
 */
public class RepositoryResultCursor<T> implements Cursor<T> {

	private RepositoryResult<T> result;

	public RepositoryResultCursor(RepositoryResult<T> result) {
		this.result = result;
	}

	public void close()
		throws StoreException
	{
		result.close();
	}

	public T next()
		throws StoreException
	{
		if (result.hasNext())
			return result.next();
		return null;
	}

}
