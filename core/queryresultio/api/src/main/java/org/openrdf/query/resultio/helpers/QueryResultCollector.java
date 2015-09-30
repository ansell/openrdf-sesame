/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.query.resultio.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQueryResultHandler;
import org.openrdf.query.QueryResultHandler;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;

/**
 * An implementation of the {@link QueryResultHandler} interface that is able to
 * collect a single result from either Boolean or Tuple results simultaneously.
 * <p>
 * The {@link List}s that are returned by this interface are immutable.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public class QueryResultCollector implements QueryResultHandler, TupleQueryResultHandler, BooleanQueryResultHandler {

	private boolean hasBooleanSet = false;

	private Boolean value = null;

	private boolean endQueryResultFound = false;

	private List<String> bindingNames = Collections.emptyList();

	private List<BindingSet> bindingSets = Collections.emptyList();

	private List<String> links = new ArrayList<String>();

	public QueryResultCollector() {
	}

	@Override
	public void handleBoolean(boolean value)
		throws QueryResultHandlerException
	{
		hasBooleanSet = true;
		this.value = value;
	}

	@Override
	public void startQueryResult(List<String> bindingNames)
		throws TupleQueryResultHandlerException
	{
		endQueryResultFound = false;
		this.bindingNames = Collections.unmodifiableList(new ArrayList<String>(bindingNames));
		bindingSets = new ArrayList<BindingSet>();
	}

	@Override
	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
		endQueryResultFound = false;
		bindingSets.add(bindingSet);
	}

	@Override
	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		endQueryResultFound = true;
		// the binding sets cannot be modified after this point without a call to
		// startQueryResult which will reset the bindingsets
		bindingSets = Collections.unmodifiableList(bindingSets);
		// reset the start query result found variable at this point
	}

	@Override
	public void handleLinks(List<String> linkUrls)
		throws QueryResultHandlerException
	{
		this.links.addAll(linkUrls);
	}

	/**
	 * Determines whether {@link #handleBoolean(boolean)} was called for this
	 * collector.
	 * 
	 * @return True if there was a boolean handled by this collector.
	 * @since 2.7.0
	 */
	public boolean getHandledBoolean() {
		return hasBooleanSet;
	}

	/**
	 * If {@link #getHandledBoolean()} returns true this method returns the
	 * boolean that was last found using {@link #handleBoolean(boolean)}
	 * <p>
	 * If {@link #getHandledBoolean()} returns false this method throws a
	 * {@link QueryResultHandlerException} indicating that a response could not
	 * be provided.
	 * 
	 * @return The boolean value that was collected.
	 * @throws QueryResultHandlerException
	 *         If there was no boolean value collected.
	 * @since 2.7.0
	 */
	public boolean getBoolean()
		throws QueryResultHandlerException
	{
		if (!hasBooleanSet) {
			throw new QueryResultHandlerException("Did not collect a boolean value");
		}
		else {
			return this.value;
		}
	}

	/**
	 * Determines whether {@link #endQueryResult()} was called after the last
	 * calls to {@link #startQueryResult(List)} and optionally calls to
	 * {@link #handleSolution(BindingSet)}.
	 * 
	 * @return True if there was a call to {@link #endQueryResult()} after the
	 *         last calls to {@link #startQueryResult(List)} and
	 *         {@link #handleSolution(BindingSet)}.
	 * @since 2.7.0
	 */
	public boolean getHandledTuple() {
		return endQueryResultFound;
	}

	/**
	 * Returns a collection of binding names collected.
	 * 
	 * @return An immutable list of {@link String}s that were collected as the
	 *         binding names.
	 * @throws QueryResultHandlerException
	 *         If the tuple results set was not successfully collected, as
	 *         signalled by a call to {@link #endQueryResult()}.
	 * @since 2.7.0
	 */
	public List<String> getBindingNames()
		throws QueryResultHandlerException
	{
		if (!endQueryResultFound) {
			throw new QueryResultHandlerException("Did not successfully collect a tuple results set.");
		}
		else {
			return bindingNames;
		}
	}

	/**
	 * @return An immutable list of {@link BindingSet}s that were collected as
	 *         the tuple results.
	 * @throws QueryResultHandlerException
	 *         If the tuple results set was not successfully collected, as
	 *         signalled by a call to {@link #endQueryResult()}.
	 * @since 2.7.0
	 */
	public List<BindingSet> getBindingSets()
		throws QueryResultHandlerException
	{
		if (!endQueryResultFound) {
			throw new QueryResultHandlerException("Did not successfully collect a tuple results set.");
		}
		else {
			return bindingSets;
		}
	}

	/**
	 * @return A list of links accumulated from calls to
	 *         {@link #handleLinks(List)}.
	 * @since 2.7.0
	 */
	public List<String> getLinks() {
		return Collections.unmodifiableList(links);
	}
}
