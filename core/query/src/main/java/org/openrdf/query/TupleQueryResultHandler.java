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

/**
 * An interface defining methods related to handling sequences of Solutions.
 */
public interface TupleQueryResultHandler {

	/**
	 * Indicates the start of a sequence of Solutions. The supplied bindingNames
	 * are an indication of the values that are in the Solutions. For example, a
	 * SeRQL query like <tt>select X, Y from {X} P {Y} </tt> will have binding
	 * names <tt>X</tt> and <tt>Y</tt>.
	 * 
	 * @param bindingNames
	 *        An ordered set of binding names.
	 */
	public void startQueryResult(List<String> bindingNames)
		throws TupleQueryResultHandlerException;

	/**
	 * Indicates the end of a sequence of solutions.
	 */
	public void endQueryResult()
		throws TupleQueryResultHandlerException;

	/**
	 * Handles a solution.
	 */
	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException;
}
