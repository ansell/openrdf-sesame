/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.evaluation;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.results.Cursor;
import org.openrdf.results.TupleResult;
import org.openrdf.store.StoreException;

/**
 * Wrap a TupleQueryResult as a Cursor.
 * 
 * @author James Leigh
 */
public class TupleQueryResultCursor implements Cursor<BindingSet> {

	private TupleResult result;

	private BindingSet bindings;

	public TupleQueryResultCursor(TupleResult result, BindingSet bindings) {
		this.result = result;
		this.bindings = bindings;
	}

	public void close()
		throws StoreException
	{
		result.close();
	}

	public BindingSet next()
		throws StoreException
	{
		if (!result.hasNext())
			return null;
		QueryBindingSet set = new QueryBindingSet(result.next());
		set.addAll(bindings);
		return set;
	}

}
