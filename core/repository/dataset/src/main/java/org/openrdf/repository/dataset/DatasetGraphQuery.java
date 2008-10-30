/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.dataset;

import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.repository.sail.SailGraphQuery;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
class DatasetGraphQuery extends DatasetQuery implements GraphQuery {

	protected DatasetGraphQuery(DatasetRepositoryConnection con, SailGraphQuery sailQuery) {
		super(con, sailQuery);
	}

	public GraphQueryResult evaluate()
		throws StoreException
	{
		con.loadDataset(sailQuery.getActiveDataset());
		return ((GraphQuery)sailQuery).evaluate();
	}

	public void evaluate(RDFHandler handler)
		throws StoreException, RDFHandlerException
	{
		con.loadDataset(sailQuery.getActiveDataset());
		((GraphQuery)sailQuery).evaluate(handler);
	}
}
