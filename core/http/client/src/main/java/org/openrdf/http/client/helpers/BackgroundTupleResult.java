/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.openrdf.cursor.QueueCursor;
import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.TupleQueryResultParser;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.ResultImpl;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class BackgroundTupleResult extends ResultImpl<BindingSet> implements TupleResult, Runnable, TupleQueryResultHandler {

	private volatile boolean closed;

	private volatile Thread parserThread;

	private TupleQueryResultParser parser;

	private InputStream in;

	private HTTPConnection connection;

	private QueueCursor<BindingSet> queue;

	private List<String> bindingNames;

	private CountDownLatch bindingNamesReady = new CountDownLatch(1);

	public BackgroundTupleResult(TupleQueryResultParser parser, InputStream in, HTTPConnection connection) {
		this(new QueueCursor<BindingSet>(10), parser, in, connection);
	}

	public BackgroundTupleResult(QueueCursor<BindingSet> queue, TupleQueryResultParser parser, InputStream in, HTTPConnection connection) {
		super(queue);
		this.queue = queue;
		this.parser = parser;
		this.in = in;
		this.connection = connection;
	}

	public synchronized void close()
		throws StoreException
	{
		closed = true;
		if (parserThread != null) {
			parserThread.interrupt();
		}
	}

	public List<String> getBindingNames()
		throws StoreException
	{
		try {
			bindingNamesReady.await();
			return bindingNames;
		}
		catch (InterruptedException e) {
			throw new StoreException(e);
		}
	}

	public void run() {
		parserThread = Thread.currentThread();
		try {
			parser.setTupleQueryResultHandler(this);
			parser.parse(in);
			// release connection back into pool if all results have been read
			connection.release();
		}
		catch (TupleQueryResultHandlerException e) {
			// parsing cancelled or interrupted
			connection.abort();
		}
		catch (QueryResultParseException e) {
			queue.toss(e);
			connection.abort();
		}
		catch (IOException e) {
			queue.toss(e);
			connection.abort();
		}
		finally {
			parserThread = null;
			queue.done();
		}
	}

	public void startQueryResult(List<String> bindingNames)
		throws TupleQueryResultHandlerException
	{
		this.bindingNames = bindingNames;
		bindingNamesReady.countDown();
	}

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

	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		// no-op
	}

}
