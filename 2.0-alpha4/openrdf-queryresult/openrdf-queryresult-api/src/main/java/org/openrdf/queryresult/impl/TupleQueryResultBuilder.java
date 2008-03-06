/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.queryresult.impl;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.queryresult.TupleQueryResult;
import org.openrdf.queryresult.TupleQueryResultHandlerException;
import org.openrdf.queryresult.Solution;
import org.openrdf.queryresult.helpers.QueryResultHandlerBase;


/**
 * A TupleQueryResultHandler that can be used to create a TupleQueryResult object.
 */
public class TupleQueryResultBuilder extends QueryResultHandlerBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private TupleQueryResultImpl _queryResult;

	/*-----------*
	 * Methods *
	 *-----------*/

	@Override
	public void startQueryResult(List<String> bindingNames, boolean distinct, boolean ordered)
		throws TupleQueryResultHandlerException
	{
		_queryResult = new TupleQueryResultImpl(new ArrayList<String>(bindingNames));
		_queryResult.setDistinct(distinct);
		_queryResult.setOrdered(ordered);
	}

	@Override
	public void handleSolution(Solution solution)
		throws TupleQueryResultHandlerException
	{
		_queryResult.addSolution(solution);
	}

	public TupleQueryResult getQueryResult() {
		return _queryResult;
	}
}
