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

	private TupleQueryResultParser parser;

	private InputStream in;

	private QueueCursor<BindingSet> queue;

	private List<String> bindingNames;

	private CountDownLatch bindingNamesReady = new CountDownLatch(1);

	public BackgroundTupleResult(TupleQueryResultParser parser, InputStream in) {
		this(new QueueCursor<BindingSet>(10), parser, in);
	}

	public BackgroundTupleResult(QueueCursor<BindingSet> queue, TupleQueryResultParser parser, InputStream in)
	{
		super(Collections.<String>emptyList(), queue);
		this.queue = queue;
		this.parser = parser;
		this.in = in;
	}

	@Override
	protected synchronized void handleClose()
		throws QueryEvaluationException
	{
		try {
			try {
				closed = true;
				super.handleClose();
			} finally {
				in.close();
			}
		}
		catch (IOException e) {
			throw new QueryEvaluationException(e);
		}
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
		try {
			try {
				parser.setQueryResultHandler(this);
				parser.parseQueryResult(in);
			} finally {
				in.close();
			}
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
		try {
			queue.put(bindingSet);
		}
		catch (InterruptedException e) {
			throw new TupleQueryResultHandlerException(e);
		}
		if (closed)
			throw new TupleQueryResultHandlerException("Result closed");
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
		// ignore
	}
}
