/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.impl;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerBase;
import org.openrdf.query.TupleQueryResultHandlerException;


/**
 * A TupleQueryResultHandler that can be used to create a TupleQueryResult object.
 */
public class TupleQueryResultBuilder extends TupleQueryResultHandlerBase {

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
	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
		_queryResult.addSolution(bindingSet);
	}

	public TupleQueryResult getQueryResult() {
		return _queryResult;
	}
}
