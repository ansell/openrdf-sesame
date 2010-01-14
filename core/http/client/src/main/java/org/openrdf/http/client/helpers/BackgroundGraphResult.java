/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.openrdf.cursor.QueueCursor;
import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.model.Statement;
import org.openrdf.result.GraphResult;
import org.openrdf.result.impl.ModelResultImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class BackgroundGraphResult extends ModelResultImpl implements GraphResult, Runnable, RDFHandler {

	private volatile boolean closed;

	private volatile Thread parserThread;

	private final RDFParser parser;

	private final InputStream in;

	private final String baseURI;

	private final HTTPConnection connection;

	private final CountDownLatch namespacesReady = new CountDownLatch(1);

	private final Map<String, String> namespaces = new ConcurrentHashMap<String, String>();

	private final QueueCursor<Statement> queue;

	public BackgroundGraphResult(RDFParser parser, InputStream in, String baseURI, HTTPConnection connection) {
		this(new QueueCursor<Statement>(10), parser, in, baseURI, connection);
	}

	public BackgroundGraphResult(QueueCursor<Statement> queue, RDFParser parser, InputStream in,
			String baseURI, HTTPConnection connection)
	{
		super(queue);
		this.queue = queue;
		this.parser = parser;
		this.in = in;
		this.baseURI = baseURI;
		this.connection = connection;
	}

	@Override
	public void close()
		throws StoreException
	{
		closed = true;
		if (parserThread != null) {
			parserThread.interrupt();
		}
		super.close();
	}

	public void run() {
		parserThread = Thread.currentThread();
		try {
			parser.setRDFHandler(this);
			parser.parse(in, baseURI);
			// release connection back into pool if all results have been read
			connection.release();
		}
		catch (RDFHandlerException e) {
			// parsing was cancelled or interrupted
			connection.abort();
		}
		catch (RDFParseException e) {
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
			namespacesReady.countDown();
		}
	}

	public void startRDF()
		throws RDFHandlerException
	{
		// no-op
	}

	@Override
	public Map<String, String> getNamespaces()
		throws StoreException
	{
		try {
			namespacesReady.await();
			queue.checkException();
			return namespaces;
		}
		catch (InterruptedException e) {
			throw new StoreException(e);
		}
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
		namespacesReady.countDown();
		if (closed) {
			throw new RDFHandlerException("Result closed");
		}
		try {
			queue.put(st);
		}
		catch (InterruptedException e) {
			throw new RDFHandlerException(e);
		}
	}

	public void endRDF()
		throws RDFHandlerException
	{
		// no-op
	}
}
