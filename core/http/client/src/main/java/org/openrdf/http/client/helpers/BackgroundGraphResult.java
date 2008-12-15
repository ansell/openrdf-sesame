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
import java.util.Map;
import java.util.Set;
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
 * @author James Leigh
 */
public class BackgroundGraphResult implements GraphQueryResult, Runnable, RDFHandler {

	private volatile boolean aborted;

	private URI uri = new URIImpl("urn:stop");

	private Statement afterLast = new StatementImpl(uri, uri, uri);

	private String baseURI;

	private volatile boolean completed;

	private HTTPConnection connection;

	private volatile Throwable exception;

	private InputStream in;

	private Map<String, String> namespaces = new ConcurrentHashMap<String, String>();

	private Statement next;

	private RDFParser parser;

	private volatile Thread parserThread;

	private BlockingQueue<Statement> queue = new ArrayBlockingQueue<Statement>(10);

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
		aborted = true;
		if (parserThread != null) {
			parserThread.interrupt();
		}
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
		if (aborted)
			throw new RDFHandlerException("Result closed");
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

	public <C extends Collection<? super Statement>> C addTo(C collection)
		throws StoreException
	{
		Statement st;
		while ((st = next()) != null) {
			collection.add(st);
		}
		return collection;
	}

	public List<Statement> asList()
		throws StoreException
	{
		return addTo(new ArrayList<Statement>());
	}

	public Set<Statement> asSet()
		throws StoreException
	{
		return addTo(new HashSet<Statement>());
	}

	public void run() {
		parserThread = Thread.currentThread();
		try {
			parser.parse(in, baseURI);
			// release connection back into pool if all results have been read
			connection.release();
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

	public void startRDF()
		throws RDFHandlerException
	{
		// no-op
	}

}
