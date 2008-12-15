/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.impl;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleResult;
import org.openrdf.query.TupleQueryResultHandlerBase;
import org.openrdf.query.TupleQueryResultHandlerException;

/**
 * A TupleQueryResultHandler that can be used to create a TupleQueryResult object.
 */
public class TupleQueryResultBuilder extends TupleQueryResultHandlerBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private List<String>	bindingNames;
	
	private List<BindingSet> bindingSetList;
	
	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void startQueryResult(List<String> bindingNames)
		throws TupleQueryResultHandlerException
	{
		this.bindingNames = bindingNames;
		bindingSetList = new ArrayList<BindingSet>();
	}

	@Override
	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
		bindingSetList.add(bindingSet);
	}

	public TupleResult getQueryResult() {
		return new TupleResultImpl(bindingNames, bindingSetList);
	}
}
