/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;

/**
 * Base class for {@link TupleQueryResultHandler}s with dummy implementations
 * of all methods. This class is a useful superclass for classes that implement
 * only one or two TupleQueryResultHandler methods.
 */
public class TupleQueryResultHandlerBase implements TupleQueryResultHandler {

	public void startQueryResult(List<String> bindingNames)
		throws TupleQueryResultHandlerException
	{
	}

	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
	}

	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
	}

}
