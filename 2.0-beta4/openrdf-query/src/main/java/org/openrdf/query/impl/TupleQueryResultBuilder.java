/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
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

	private List<String>	_bindingNames;
	
	private List<BindingSet> _bindingSetList;
	
	private boolean _distinct;
	
	private boolean _ordered;

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void startQueryResult(List<String> bindingNames, boolean distinct, boolean ordered)
		throws TupleQueryResultHandlerException
	{
		_bindingNames = bindingNames;
		_bindingSetList = new ArrayList<BindingSet>();
		_distinct = distinct;
		_ordered = ordered;
	}

	@Override
	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
		_bindingSetList.add(bindingSet);
	}

	public TupleQueryResult getQueryResult() {
		TupleQueryResultImpl _queryResult = new TupleQueryResultImpl(_bindingNames, _bindingSetList);
		_queryResult.setDistinct(_distinct);
		_queryResult.setOrdered(_ordered);
		return _queryResult;
	}
}
