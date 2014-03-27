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
package org.openrdf.http.client;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.httpclient.HttpMethod;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.TupleQueryResultImpl;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.TupleQueryResultParser;

/**
 * Provides concurrent access to tuple results as they are being parsed.
 * 
 * @author James Leigh
 */
public class BackgroundTupleResult extends TupleQueryResultImpl implements Runnable, TupleQueryResultHandler {

	private volatile boolean closed;

	private volatile Thread parserThread;

	/** lock needed for using/dereferening parserThread. */
	private final Object parserThreadLock = new Object();
	
	private TupleQueryResultParser parser;

	private InputStream in;

	private HttpMethod method;

	private QueueCursor<BindingSet> queue;

	private List<String> bindingNames;

	private List<String> links;

	private CountDownLatch bindingNamesReady = new CountDownLatch(1);

	private CountDownLatch linksReady = new CountDownLatch(1);

	public BackgroundTupleResult(TupleQueryResultParser parser, InputStream in, HttpMethod connection) {
		this(new QueueCursor<BindingSet>(10), parser, in, connection);
	}

	public BackgroundTupleResult(QueueCursor<BindingSet> queue, TupleQueryResultParser parser, InputStream in,
			HttpMethod connection)
	{
		super(Collections.<String> emptyList(), queue);
		this.queue = queue;
		this.parser = parser;
		this.in = in;
		this.method = connection;
	}

	@Override
	protected void handleClose()
		throws QueryEvaluationException
	{
		synchronized (parserThreadLock) {
			closed = true;
			final Thread thread = parserThread;
			if (thread != null) {
				thread.interrupt();
			}
		}
		super.handleClose();
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
		boolean completed = false;
		parserThread = Thread.currentThread();
		try {
			parser.setQueryResultHandler(this);
			parser.parseQueryResult(in);
			// release connection back into pool if all results have been read
			method.releaseConnection();
			completed = true;
		}
		catch (QueryResultHandlerException e) {
			// parsing cancelled or interrupted
		}
		catch (QueryResultParseException e) {
			queue.toss(e);
		}
		catch (IOException e) {
			queue.toss(e);
		}
		finally {
			synchronized (parserThreadLock) {
				parserThread = null;
			}
			queue.done();
			bindingNamesReady.countDown();
			if (!completed) {
				method.abort();
				method.releaseConnection();
			}
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
	public void handleBoolean(boolean value)
		throws QueryResultHandlerException
	{
		throw new UnsupportedOperationException("Cannot handle boolean results");
	}

	@Override
	public void handleLinks(List<String> linkUrls)
		throws QueryResultHandlerException
	{
		this.links = linkUrls;
		linksReady.countDown();
	}
}
