/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.dataset;

import org.openrdf.StoreException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.sail.SailTupleQuery;

/**
 * @author Arjohn Kampman
 */
class DatasetTupleQuery extends DatasetQuery implements TupleQuery {

	protected DatasetTupleQuery(DatasetRepositoryConnection con, SailTupleQuery sailQuery) {
		super(con, sailQuery);
	}

	public TupleQueryResult evaluate()
		throws StoreException
	{
		con.loadDataset(sailQuery.getActiveDataset());
		return ((TupleQuery)sailQuery).evaluate();
	}

	public void evaluate(TupleQueryResultHandler handler)
		throws StoreException, TupleQueryResultHandlerException
	{
		con.loadDataset(sailQuery.getActiveDataset());
		((TupleQuery)sailQuery).evaluate(handler);
	}
}
