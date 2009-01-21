/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.result.TupleResult;
import org.openrdf.store.StoreException;

public interface TupleQuery extends Query {

	public TupleResult evaluate()
		throws StoreException;

	public <H extends TupleQueryResultHandler> H evaluate(H handler)
		throws StoreException, TupleQueryResultHandlerException;
}
