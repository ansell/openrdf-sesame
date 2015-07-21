/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.repository.dataset;

import org.openrdf.query.QueryEvaluationException;
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
		throws QueryEvaluationException
	{
		con.loadDataset(sailQuery.getActiveDataset());
		return ((TupleQuery)sailQuery).evaluate();
	}

	public void evaluate(TupleQueryResultHandler handler)
		throws QueryEvaluationException, TupleQueryResultHandlerException
	{
		con.loadDataset(sailQuery.getActiveDataset());
		((TupleQuery)sailQuery).evaluate(handler);
	}
}
