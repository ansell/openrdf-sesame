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
package org.openrdf.repository.sail;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * @author Arjohn Kampman
 */
public class SailBooleanQuery extends SailQuery implements BooleanQuery {

	protected SailBooleanQuery(ParsedBooleanQuery tupleQuery, SailRepositoryConnection sailConnection) {
		this(tupleQuery, sailConnection.getSailConnection());
	}

	protected SailBooleanQuery(ParsedBooleanQuery tupleQuery, SailConnection sailConnection) {
		super(tupleQuery, sailConnection);
	}

	@Override
	public ParsedBooleanQuery getParsedQuery() {
		return (ParsedBooleanQuery)super.getParsedQuery();
	}

	@Override
	public boolean evaluate()
		throws QueryEvaluationException
	{
		ParsedBooleanQuery parsedBooleanQuery = getParsedQuery();
		TupleExpr tupleExpr = parsedBooleanQuery.getTupleExpr();
		Dataset dataset = getDataset();
		if (dataset == null) {
			// No external dataset specified, use query's own dataset (if any)
			dataset = parsedBooleanQuery.getDataset();
		}

		try {
			SailConnection sailCon = getSailConnection();

			CloseableIteration<? extends BindingSet, QueryEvaluationException> bindingsIter;
			bindingsIter = sailCon.evaluate(tupleExpr, dataset, getBindings(), getIncludeInferred());

			bindingsIter = enforceMaxQueryTime(bindingsIter);

			try {
				return bindingsIter.hasNext();
			}
			finally {
				bindingsIter.close();
			}
		}
		catch (SailException e) {
			throw new QueryEvaluationException(e.getMessage(), e);
		}
	}
}
