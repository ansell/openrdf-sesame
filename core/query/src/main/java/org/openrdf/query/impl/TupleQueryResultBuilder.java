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
package org.openrdf.query.impl;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.AbstractTupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;

/**
 * A TupleQueryResultHandler that can be used to create a TupleQueryResult
 * object.
 */
public class TupleQueryResultBuilder extends AbstractTupleQueryResultHandler {

	/*-----------*
	 * Variables *
	 *-----------*/

	private List<String> bindingNames;

	private List<BindingSet> bindingSetList = new ArrayList<BindingSet>();

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

	public TupleQueryResult getQueryResult() {
		return new IteratingTupleQueryResult(bindingNames, bindingSetList);
	}
}
