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
 * The super class of all handlers for processing query results.
 * <p>
 * This interface contains methods for optionally processing both boolean and
 * tuple results sets simultaneously, but there are no guarantees that an
 * implementation will be able to process these values together. If a method is
 * not supported then an {@link UnsupportedOperationException} will be thrown to
 * indicate this failure. This failure may be prevented by checking for whether
 * the class implements {@link BooleanQueryResultHandler} or
 * {@link TupleQueryResultHandler}, for boolean and tuple results support
 * respectively.
 * <p>
 * If both boolean and tuple results are supported but they are not able to to
 * be processed simultaneously, then a checked exception, either
 * {@link BooleanQueryResultHandlerException} or
 * {@link TupleQueryResultHandlerException}, will be thrown to indicate this
 * failure when the relevant methods are called.
 * 
 * @since 2.7.0
 * @author Peter Ansell
 */
public interface QueryResultHandler {

	/**
	 * Handles the specified boolean value.
	 * 
	 * @param value
	 *        The boolean value to handle.
	 * @throws QueryResultHandlerException
	 *         If there was an error during the handling of this value. This
	 *         exception may be thrown if the {@link #startQueryResult(List)},
	 *         {@link #handleSolution(BindingSet)} or {@link #endQueryResult()}
	 *         methods were called before this method was called, and the handler
	 *         cannot process both boolean and tuple results simultaneously.
	 * @throws UnsupportedOperationException
	 *         If this method is not supported
	 * @since 2.7.0
	 */
	void handleBoolean(boolean value)
		throws QueryResultHandlerException;

	/**
	 * Handles the links elements which are present in SPARQL Results JSON and
	 * SPARQL Results XML documents in the header.
	 * <p>
	 * NOTE: If the format does not support links, it must silently ignore a call
	 * to this method.
	 * <p>
	 * An accumulating handler should accumulate these links.
	 * 
	 * @see <a
	 *      href="http://www.w3.org/TR/sparql11-results-json/#select-link">"link"</a>
	 * @param linkUrls
	 *        The URLs of the links to handle.
	 * @throws QueryResultHandlerException
	 *         If there was an error handling the set of link URLs. This error is
	 *         not thrown in cases where links are not supported.
	 * @since 2.7.0
	 */
	void handleLinks(List<String> linkUrls)
		throws QueryResultHandlerException;

	/**
	 * Indicates the start of a sequence of Solutions. The supplied bindingNames
	 * are an indication of the values that are in the Solutions. For example, a
	 * SeRQL query like <tt>select X, Y from {X} P {Y} </tt> will have binding
	 * names <tt>X</tt> and <tt>Y</tt>.
	 * 
	 * @param bindingNames
	 *        An ordered set of binding names.
	 * @throws TupleQueryResultHandlerException
	 *         If there was an error during the starting of the query result
	 *         handler. This exception may be thrown if the
	 *         {@link #handleBoolean(boolean)} method was called before this
	 *         method and the handler cannot process both boolean and tuple
	 *         results simultaneously.
	 * @throws UnsupportedOperationException
	 *         If this method is not supported
	 * @throws IllegalStateException
	 *         If the {@link #handleSolution(BindingSet)} or
	 *         {@link #endQueryResult()} methods were called before this method
	 *         and the handler cannot process multiple sets of tuple results
	 *         simultaneously.
	 */
	public void startQueryResult(List<String> bindingNames)
		throws TupleQueryResultHandlerException;

	/**
	 * Indicates the end of a sequence of solutions.
	 * 
	 * @throws TupleQueryResultHandlerException
	 *         If there was an error during the ending of the query result
	 *         handler. This exception may be thrown if the
	 *         {@link #handleBoolean(boolean)} method was called before this
	 *         method and the handler cannot process both boolean and tuple
	 *         results simultaneously.
	 * @throws UnsupportedOperationException
	 *         If this method is not supported
	 * @throws IllegalStateException
	 *         If the {@link #endQueryResult()} was previously called for this
	 *         handler or {@link #startQueryResult(List)} was NOT called before
	 *         this method.
	 */
	public void endQueryResult()
		throws TupleQueryResultHandlerException;

	/**
	 * Handles a solution.
	 * 
	 * @param bindingSet
	 *        A single set of tuple results, with binding names bound to values.
	 *        Each of the binding names in the solution must have previously been
	 *        registered with the {@link #startQueryResult(List)} method.
	 * @throws TupleQueryResultHandlerException
	 *         If there was an error during the handling of the query solution.
	 *         This exception may be thrown if the
	 *         {@link #handleBoolean(boolean)} method was called before this
	 *         method and the handler cannot process both boolean and tuple
	 *         results simultaneously.
	 * @throws UnsupportedOperationException
	 *         If this method is not supported
	 * @throws IllegalStateException
	 *         If the {@link #endQueryResult()} method was called before this
	 *         method or {@link #startQueryResult(List)} was NOT called before
	 *         this method.
	 */
	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException;
}
