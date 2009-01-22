/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.result.TupleResult;
import org.openrdf.store.StoreException;

public interface TupleQuery extends Query {

	/**
	 * Specifies the numbers of results that should be omitted from the beginning
	 * of the query results.
	 * 
	 * @param offset
	 */
	public void setOffset(int offset);

	/**
	 * Returns the number of skipped results.
	 * 
	 * @return the numbers of results that should be omitted from the beginning
	 *         of the query results.
	 */
	public int getOffset();

	/**
	 * Specifies the maximum results that a query is allowed to return. The query
	 * stop before it exceeds the result limit. Any consecutive requests to fetch
	 * query results will result in a null value.
	 * 
	 * @param limit
	 *        The maximum number of query results. A -1 value indicates an
	 *        unlimited results (which is the default).
	 */
	public void setLimit(int limit);

	/**
	 * Returns the maximum number of query resultcs.
	 * 
	 * @return The maximum number of query results. A -1 value indicates an
	 *         unlimited results.
	 * @see #setLimit(int)
	 */
	public int getLimit();

	public TupleResult evaluate()
		throws StoreException;

	public <H extends TupleQueryResultHandler> H evaluate(H handler)
		throws StoreException, TupleQueryResultHandlerException;
}
