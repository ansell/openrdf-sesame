/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import java.util.ArrayList;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResultUtil;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.impl.TupleQueryResultImpl;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.sail.SailConnection;
import org.openrdf.StoreException;

/**
 * @author Arjohn Kampman
 */
public class SailTupleQuery extends SailQuery implements TupleQuery {

	protected SailTupleQuery(ParsedTupleQuery tupleQuery, SailRepositoryConnection sailConnection) {
		super(tupleQuery, sailConnection);
	}

	@Override
	public ParsedTupleQuery getParsedQuery() {
		return (ParsedTupleQuery)super.getParsedQuery();
	}

	public TupleQueryResult evaluate()
		throws QueryEvaluationException
	{
		TupleExpr tupleExpr = getParsedQuery().getTupleExpr();

		try {
			SailConnection sailCon = getConnection().getSailConnection();
			CloseableIteration<? extends BindingSet, QueryEvaluationException> bindingsIter = sailCon.evaluate(
					tupleExpr, getActiveDataset(), getBindings(), getIncludeInferred());

			return new TupleQueryResultImpl(new ArrayList<String>(tupleExpr.getBindingNames()), bindingsIter);
		}
		catch (StoreException e) {
			throw new QueryEvaluationException(e.getMessage(), e);
		}
	}

	public void evaluate(TupleQueryResultHandler handler)
		throws QueryEvaluationException, TupleQueryResultHandlerException
	{
		TupleQueryResult queryResult = evaluate();
		QueryResultUtil.report(queryResult, handler);
	}
}
