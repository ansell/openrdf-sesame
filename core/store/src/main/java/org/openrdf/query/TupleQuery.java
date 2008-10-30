/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.store.StoreException;

public interface TupleQuery extends Query {

	public TupleQueryResult evaluate()
		throws StoreException;

	public void evaluate(TupleQueryResultHandler handler)
		throws StoreException, TupleQueryResultHandlerException;
}
