/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http.helpers;

import org.openrdf.model.Statement;
import org.openrdf.results.Cursor;
import org.openrdf.results.GraphResult;
import org.openrdf.store.StoreException;


/**
 *
 * @author James Leigh
 */
public class GraphQueryResultCursor implements Cursor<Statement> {
	private GraphResult result;

	public GraphQueryResultCursor(GraphResult result) {
		this.result = result;
	}

	public void close()
		throws StoreException
	{
		result.close();
	}

	public Statement next()
		throws StoreException
	{
		if (result.hasNext())
			return result.next();
		return null;
	}

}
