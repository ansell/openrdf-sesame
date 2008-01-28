/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.dataset;

import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.sail.SailBooleanQuery;

/**
 * 
 * @author Arjohn Kampman
 */
class DatasetBooleanQuery extends DatasetQuery implements BooleanQuery {

	protected DatasetBooleanQuery(DatasetRepositoryConnection con, SailBooleanQuery sailQuery) {
		super(con, sailQuery);
	}

	public boolean evaluate()
		throws QueryEvaluationException
	{
		con.loadDataset(sailQuery.getActiveDataset());
		return ((BooleanQuery)sailQuery).evaluate();
	}
}
