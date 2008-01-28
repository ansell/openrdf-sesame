/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.dataset;

import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.sail.SailGraphQuery;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * @author Arjohn Kampman
 */
class DatasetGraphQuery extends DatasetQuery implements GraphQuery {

	protected DatasetGraphQuery(DatasetRepositoryConnection con, SailGraphQuery sailQuery) {
		super(con, sailQuery);
	}

	public GraphQueryResult evaluate()
		throws QueryEvaluationException
	{
		con.loadDataset(sailQuery.getActiveDataset());
		return ((GraphQuery)sailQuery).evaluate();
	}

	public void evaluate(RDFHandler handler)
		throws QueryEvaluationException, RDFHandlerException
	{
		con.loadDataset(sailQuery.getActiveDataset());
		((GraphQuery)sailQuery).evaluate(handler);
	}
}
