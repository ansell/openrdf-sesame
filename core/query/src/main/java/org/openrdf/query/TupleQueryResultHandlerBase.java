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
package org.openrdf.query;

import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;

/**
 * Base class for {@link TupleQueryResultHandler}s with dummy implementations of
 * all methods. This class is a useful superclass for classes that implement
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

	@Override
	public void handleBoolean(boolean value)
		throws QueryResultHandlerException
	{
		// This is a base class for handling tuple results
		throw new UnsupportedOperationException("Cannot handle boolean results");
	}
}
