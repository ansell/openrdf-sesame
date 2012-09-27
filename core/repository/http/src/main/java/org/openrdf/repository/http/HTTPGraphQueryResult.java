/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.model.Statement;
import org.openrdf.query.Binding;
import org.openrdf.query.Dataset;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.GraphQueryResultImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;

/**
 * Provides concurrent access to statements as they are being parsed.
 * 
 * @author James Leigh
 * @author Jeen Broekstra
 */
public class HTTPGraphQueryResult extends GraphQueryResultImpl implements GraphQueryResult, Runnable,
		RDFHandler
{

	private volatile boolean closed;

	private volatile Thread parserThread;

	private String baseURI;

	private CountDownLatch namespacesReady = new CountDownLatch(1);

	private Map<String, String> namespaces = new ConcurrentHashMap<String, String>();

	private QueueCursor<Statement> queue;

	private HTTPClient httpClient;

	private QueryLanguage queryLanguage;

	private String queryString;

	private Binding[] bindings;

	private boolean includeInferred;

	private Dataset dataset;

	public HTTPGraphQueryResult(HTTPClient httpClient, QueryLanguage queryLanguage, String queryString,
			String baseURI, Dataset dataset, boolean includeInferred, Binding... bindings)
	{
		this(new QueueCursor<Statement>(10), httpClient, queryLanguage, queryString, baseURI, dataset,
				includeInferred, bindings);
	}

	public HTTPGraphQueryResult(QueueCursor<Statement> queue, HTTPClient httpClient,
			QueryLanguage queryLanguage, String queryString, String baseURI, Dataset dataset,
			boolean includeInferred, Binding... bindings)
	{
		super(new HashMap<String, String>(), queue);

		this.queue = queue;
		this.httpClient = httpClient;
		this.queryLanguage = queryLanguage;
		this.queryString = queryString;
		this.baseURI = baseURI;
		this.dataset = dataset;
		this.includeInferred = includeInferred;
		this.bindings = bindings;

	}

	public boolean hasNext()
		throws QueryEvaluationException
	{
		return queue.hasNext();
	}

	public Statement next()
		throws QueryEvaluationException
	{
		return queue.next();
	}

	public void remove()
		throws QueryEvaluationException
	{
		queue.remove();
	}

	@Override
	protected void handleClose()
		throws QueryEvaluationException
	{
		closed = true;
		if (parserThread != null) {
			parserThread.interrupt();
		}
		queue.close();
		super.handleClose();
	}

	public void run() {
		parserThread = Thread.currentThread();
		try {
			// TODO set max query time?
			httpClient.sendGraphQuery(queryLanguage, queryString, baseURI, dataset, includeInferred, 0, this,
					bindings);
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
		catch (RDFHandlerException e) {
			queue.toss(e);
		}
		finally {
			parserThread = null;
			queue.done();
		}
	}

	public void startRDF()
		throws RDFHandlerException
	{
		// no-op
	}

	public Map<String, String> getNamespaces() {
		try {
			namespacesReady.await();
			return namespaces;
		}
		catch (InterruptedException e) {
			throw new UndeclaredThrowableException(e);
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
		if (closed)
			throw new RDFHandlerException("Result closed");
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
		namespacesReady.countDown();
	}

}
