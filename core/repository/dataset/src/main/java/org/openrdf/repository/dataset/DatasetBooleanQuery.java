/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.dataset;

import org.openrdf.query.BooleanQuery;
import org.openrdf.repository.sail.SailBooleanQuery;
import org.openrdf.results.BooleanResult;
import org.openrdf.store.StoreException;

/**
 * 
 * @author Arjohn Kampman
 */
class DatasetBooleanQuery extends DatasetQuery implements BooleanQuery {

	protected DatasetBooleanQuery(DatasetRepositoryConnection con, SailBooleanQuery sailQuery) {
		super(con, sailQuery);
	}

	public BooleanResult evaluate()
		throws StoreException
	{
		con.loadDataset(sailQuery.getActiveDataset());
		return ((BooleanQuery)sailQuery).evaluate();
	}

	public boolean ask()
		throws StoreException
	{
		con.loadDataset(sailQuery.getActiveDataset());
		return ((BooleanQuery)sailQuery).ask();
	}
}
