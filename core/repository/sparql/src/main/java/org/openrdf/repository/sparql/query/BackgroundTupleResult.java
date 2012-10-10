/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sparql.query;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.httpclient.HttpMethod;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
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
public class BackgroundTupleResult extends TupleQueryResultImpl implements
		TupleQueryResult, Runnable, TupleQueryResultHandler {

	private volatile boolean closed;

	private volatile Thread parserThread;

	private TupleQueryResultParser parser;

	private InputStream in;

	private HttpMethod method;

	private QueueCursor<BindingSet> queue;

	private List<String> bindingNames;

	private CountDownLatch bindingNamesReady = new CountDownLatch(1);

	public BackgroundTupleResult(TupleQueryResultParser parser, InputStream in,
			HttpMethod connection) {
		this(new QueueCursor<BindingSet>(10), parser, in, connection);
	}

	public BackgroundTupleResult(QueueCursor<BindingSet> queue,
			TupleQueryResultParser parser, InputStream in, HttpMethod connection) {
		super(Collections.EMPTY_LIST, queue);
		this.queue = queue;
		this.parser = parser;
		this.in = in;
		this.method = connection;
	}

	@Override
	protected synchronized void handleClose() throws QueryEvaluationException {
		closed = true;
		if (parserThread != null) {
			parserThread.interrupt();
		}
		super.handleClose();
	}

	public List<String> getBindingNames() {
		try {
			bindingNamesReady.await();
			queue.checkException();
			return bindingNames;
		} catch (InterruptedException e) {
			throw new UndeclaredThrowableException(e);
		} catch (QueryEvaluationException e) {
			throw new UndeclaredThrowableException(e);
		}
	}

	public void run() {
		boolean completed = false;
		parserThread = Thread.currentThread();
		try {
			parser.setTupleQueryResultHandler(this);
			parser.parse(in);
			// release connection back into pool if all results have been read
			method.releaseConnection();
			completed = true;
		} catch (TupleQueryResultHandlerException e) {
			// parsing cancelled or interrupted
		} catch (QueryResultParseException e) {
			queue.toss(e);
		} catch (IOException e) {
			queue.toss(e);
		} finally {
			parserThread = null;
			queue.done();
			bindingNamesReady.countDown();
			if (!completed) {
				method.abort();
			}
		}
	}

	public void startQueryResult(List<String> bindingNames)
			throws TupleQueryResultHandlerException {
		this.bindingNames = bindingNames;
		bindingNamesReady.countDown();
	}

	public void handleSolution(BindingSet bindingSet)
			throws TupleQueryResultHandlerException {
		if (closed)
			throw new TupleQueryResultHandlerException("Result closed");
		try {
			queue.put(bindingSet);
		} catch (InterruptedException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public void endQueryResult() throws TupleQueryResultHandlerException {
		// no-op
	}

}
