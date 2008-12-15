/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client.helpers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.resultio.TupleQueryResultParser;
import org.openrdf.results.TupleResult;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class BackgroundTupleResult implements TupleResult, Runnable, TupleQueryResultHandler {

	private volatile boolean aborted;

	private BindingSet afterLast = EmptyBindingSet.getInstance();

	private volatile boolean completed;

	private volatile Throwable exception;

	private InputStream in;

	private BindingSet next;

	private TupleQueryResultParser parser;

	private volatile Thread parserThread;

	private BlockingQueue<BindingSet> queue = new ArrayBlockingQueue<BindingSet>(10);

	private HTTPConnection connection;

	private List<String> bindingNames;

	private CountDownLatch start = new CountDownLatch(1);

	public BackgroundTupleResult(TupleQueryResultParser parser, InputStream in, HTTPConnection connection) {
		this.parser = parser;
		this.in = in;
		this.connection = connection;
		parser.setTupleQueryResultHandler(this);
	}

	public synchronized void close()
		throws StoreException
	{
		aborted = true;
		if (parserThread != null) {
			parserThread.interrupt();
		}
	}

	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		// no-op
	}

	public List<String> getBindingNames()
		throws StoreException
	{
		try {
			start.await();
		}
		catch (InterruptedException e) {
			throw new StoreException(e);
		}
		return bindingNames;
	}

	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
		if (aborted)
			throw new TupleQueryResultHandlerException("Result closed");
		try {
			queue.put(bindingSet);
		}
		catch (InterruptedException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public boolean hasNext()
		throws StoreException
	{
		if (next != null)
			return true;
		next = next();
		return next != null;
	}

	public BindingSet next()
		throws StoreException
	{
		if (next != null) {
			BindingSet st = next;
			next = null;
			return st;
		}
		if (completed && queue.isEmpty())
			return null;
		if (exception != null)
			throw new StoreException(exception);
		try {
			BindingSet take = queue.take();
			if (exception != null)
				throw new StoreException(exception);
			if (take == afterLast)
				return null;
			return take;
		}
		catch (InterruptedException e) {
			if (exception != null)
				throw new StoreException(exception);
			throw new StoreException(e);
		}
	}

	public <C extends Collection<? super BindingSet>> C addTo(C collection)
		throws StoreException
	{
		BindingSet bindings;
		while ((bindings = next()) != null) {
			collection.add(bindings);
		}
		return collection;
	}

	public List<BindingSet> asList()
		throws StoreException
	{
		return addTo(new ArrayList<BindingSet>());
	}

	public Set<BindingSet> asSet()
		throws StoreException
	{
		return addTo(new HashSet<BindingSet>());
	}

	public void run() {
		parserThread = Thread.currentThread();
		try {
			parser.parse(in);
			// release connection back into pool if all results have been read
			connection.release();
		}
		catch (TupleQueryResultHandlerException e) {
			queue.clear(); // abort
			exception = e.getCause();
		}
		catch (Exception e) {
			queue.clear(); // abort
			exception = e;
		}
		try {
			if (!aborted) {
				queue.put(afterLast);
			}
		}
		catch (InterruptedException e) {
			exception = e;
		}
		synchronized (this) {
			completed = true;
			parserThread = null;
			// clear interrupted flag
			Thread.interrupted();
		}
	}

	public void startQueryResult(List<String> bindingNames)
		throws TupleQueryResultHandlerException
	{
		this.bindingNames = bindingNames;
		start.countDown();
	}

}
