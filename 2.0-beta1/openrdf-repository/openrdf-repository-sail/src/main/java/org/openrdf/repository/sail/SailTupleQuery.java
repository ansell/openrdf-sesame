/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.impl.AbstractQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.resultio.QueryResultUtil;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailTupleQueryResult;

/**
 * @author Arjohn Kampman
 */
class SailTupleQuery extends AbstractQuery implements TupleQuery {

	private ParsedTupleQuery _tupleQuery;

	private SailRepositoryConnection _repCon;

	public SailTupleQuery(ParsedTupleQuery tupleQuery,
			SailRepositoryConnection sailConnection)
	{
		_tupleQuery = tupleQuery;
		_repCon = sailConnection;
	}

	public TupleQueryResult evaluate()
		throws QueryEvaluationException
	{
		TupleExpr tupleExpr = _tupleQuery.getTupleExpr();

		try {
			SailConnection sailCon = _repCon.getSailConnection();
			CloseableIteration<? extends BindingSet, QueryEvaluationException> bindingsIter = sailCon.evaluate(
					tupleExpr, _bindings, _includeInferred);

			return new SailTupleQueryResult(tupleExpr.getBindingNames(), bindingsIter);
		}
		catch (SailException e) {
			throw new QueryEvaluationException(e);
		}
	}

	public void evaluate(TupleQueryResultHandler handler)
		throws QueryEvaluationException, TupleQueryResultHandlerException
	{
		TupleQueryResult queryResult = evaluate();
		QueryResultUtil.report(queryResult, handler);
	}
}
