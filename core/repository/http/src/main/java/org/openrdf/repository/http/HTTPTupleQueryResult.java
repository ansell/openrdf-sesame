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
package org.openrdf.repository.http;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.TupleQueryResultImpl;
import org.openrdf.repository.RepositoryException;

/**
 * Provides concurrent access to TupleQueryResults as they are being parsed.
 * 
 * @author James Leigh
 * @author Jeen Broekstra
 */
public class HTTPTupleQueryResult extends TupleQueryResultImpl implements Runnable, TupleQueryResultHandler {

	private volatile boolean closed;

	private volatile Thread parserThread;

	private HTTPClient httpClient;

	private QueueCursor<BindingSet> queue;

	private List<String> bindingNames;

	/**
	 * countdown that indicates when binding names have been processed in the
	 * query result
	 */
	private CountDownLatch bindingNamesReady = new CountDownLatch(1);

	private QueryLanguage queryLanguage;

	private String queryString;

	private Dataset dataset;

	private boolean includeInferred;

	private Binding[] bindings;

	private String baseURI;

	public HTTPTupleQueryResult(HTTPClient httpClient, QueryLanguage ql, String queryString, String baseURI,
			Dataset dataset, boolean includeInferred, Binding... bindings)
	{
		this(new QueueCursor<BindingSet>(10), httpClient, ql, queryString, baseURI, dataset, includeInferred,
				bindings);
	}

	public HTTPTupleQueryResult(QueueCursor<BindingSet> queue, HTTPClient httpClient, QueryLanguage ql,
			String queryString, String baseURI, Dataset dataset, boolean includeInferred, Binding... bindings)
	{
		super(Collections.<String> emptyList(), queue);
		this.queue = queue;
		this.httpClient = httpClient;
		this.queryLanguage = ql;
		this.queryString = queryString;
		this.baseURI = baseURI;
		this.dataset = dataset;
		this.includeInferred = includeInferred;
		this.bindings = bindings;
	}

	@Override
	public List<String> getBindingNames() {
		try {
			bindingNamesReady.await();
			queue.checkException();
			return bindingNames;
		}
		catch (InterruptedException e) {
			throw new UndeclaredThrowableException(e);
		}
		catch (QueryEvaluationException e) {
			throw new UndeclaredThrowableException(e);
		}
	}

	@Override
	public void run() {
		parserThread = Thread.currentThread();
		try {
			// TODO set max query time?
			httpClient.sendTupleQuery(queryLanguage, queryString, baseURI, dataset, includeInferred, 0, this,
					bindings);
		}
		catch (TupleQueryResultHandlerException e) {
			// parsing cancelled or interrupted
		}
		catch (IOException e) {
			queue.toss(e);
		}
		catch (UnauthorizedException e) {
			queue.toss(e);
		}
		catch (QueryInterruptedException e) {
			queue.toss(e);
		}
		catch (RepositoryException e) {
			queue.toss(e);
		}
		catch (MalformedQueryException e) {
			queue.toss(e);
		}
		finally {
			parserThread = null;
			queue.done();
			bindingNamesReady.countDown();
		}
	}

	@Override
	public void startQueryResult(List<String> bindingNames)
		throws TupleQueryResultHandlerException
	{
		this.bindingNames = bindingNames;
		bindingNamesReady.countDown();
	}

	@Override
	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
		if (closed)
			throw new TupleQueryResultHandlerException("Result closed");
		try {
			queue.put(bindingSet);
		}
		catch (InterruptedException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		// no-op
	}

	@Override
	protected synchronized void handleClose()
		throws QueryEvaluationException
	{
		closed = true;
		if (parserThread != null) {
			parserThread.interrupt();
		}
		super.handleClose();
	}

	@Override
	public void handleBoolean(boolean value)
		throws QueryResultHandlerException
	{
		throw new UnsupportedOperationException("Cannot handle boolean results");
	}

	@Override
	public void handleLinks(List<String> linkUrls)
		throws QueryResultHandlerException
	{
		// no-op
	}
}
