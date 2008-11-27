/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client.helpers;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.store.StoreException;


/**
 *
 * @author James Leigh
 */
public class BackgroundGraphResult implements GraphQueryResult, Runnable, RDFHandler {
	private URI uri = new URIImpl("urn:stop");
	private Statement afterLast = new StatementImpl(uri, uri, uri);
	private String baseURI;
	private volatile boolean completed;
	private volatile Throwable exception;
	private InputStream in;
	private Map<String, String> namespaces = new ConcurrentHashMap<String, String>();
	private Statement next;
	private RDFParser parser;
	private volatile Thread parserThread;
	private BlockingQueue<Statement> queue = new ArrayBlockingQueue<Statement>(10);
	private HTTPConnection connection;

	public BackgroundGraphResult(RDFParser parser, InputStream in, String baseURI, HTTPConnection connection) {
		this.parser = parser;
		this.in = in;
		this.baseURI = baseURI;
		this.connection = connection;
		parser.setRDFHandler(this);
	}

	public synchronized void close()
		throws StoreException
	{
		if (parserThread != null) {
			parserThread.interrupt();
		}
		connection.release();
	}

	public void endRDF()
		throws RDFHandlerException
	{
		// no-op
	}

	public Map<String, String> getNamespaces() {
		return namespaces;
	}

	public void handleComment(String comment)
		throws RDFHandlerException
	{
		// ignore
	}

	public void handleNamespace(String prefix, String uri)
		throws RDFHandlerException
	{
		namespaces.put(prefix, uri);
	}

	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		try {
			queue.put(st);
		}
		catch (InterruptedException e) {
			throw new RDFHandlerException(e);
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

	public Statement next()
		throws StoreException
	{
		if (next != null) {
			Statement st = next;
			next = null;
			return st;
		}
		if (completed && queue.isEmpty())
			return null;
		if (exception != null)
			throw new StoreException(exception);
		try {
			Statement take = queue.take();
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

	public void remove()
		throws StoreException
	{
		throw new UnsupportedOperationException();
	}

	public void run() {
		parserThread = Thread.currentThread();
		try {
			parser.parse(in, baseURI);
		}
		catch (RDFHandlerException e) {
			queue.clear(); // abort
			exception = e.getCause();
		}
		catch (Exception e) {
			queue.clear(); // abort
			exception = e;
		}
		try {
			queue.put(afterLast);
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

	public void startRDF()
		throws RDFHandlerException
	{
		// no-op
	}

}
