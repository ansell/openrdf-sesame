/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.queryresult.helpers;

import java.util.List;

import org.openrdf.queryresult.TupleQueryResultHandler;
import org.openrdf.queryresult.TupleQueryResultHandlerException;
import org.openrdf.queryresult.Solution;


/**
 * Base class for {@link TupleQueryResultHandler}s with dummy implementations of all
 * methods. This class is a useful superclass for classes that implement only
 * one or two TupleQueryResultHandler methods.
 */
public class QueryResultHandlerBase implements TupleQueryResultHandler {

	public void startQueryResult(List<String> bindingNames, boolean distinct, boolean ordered)
		throws TupleQueryResultHandlerException
	{
	}

	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
	}

	public void handleSolution(Solution solution)
		throws TupleQueryResultHandlerException
	{
	}

}
