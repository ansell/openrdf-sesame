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
import java.io.InputStreamReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import info.aduna.iteration.IterationWrapper;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;

/**
 * Provides concurrent access to statements as they are being parsed.
 * 
 * @author James Leigh
 */
public class BackgroundGraphResult extends IterationWrapper<Statement, QueryEvaluationException> implements
		GraphQueryResult, Runnable, RDFHandler
{

	private volatile boolean closed;

	private RDFParser parser;

	private Charset charset;

	private InputStream in;

	private String baseURI;

	private CountDownLatch namespacesReady = new CountDownLatch(1);

	private Map<String, String> namespaces = new ConcurrentHashMap<String, String>();

	private QueueCursor<Statement> queue;

	public BackgroundGraphResult(RDFParser parser, InputStream in, Charset charset, String baseURI)
	{
		this(new QueueCursor<Statement>(10), parser, in, charset, baseURI);
	}

	public BackgroundGraphResult(QueueCursor<Statement> queue, RDFParser parser, InputStream in,
			Charset charset, String baseURI)
	{
		super(queue);
		this.queue = queue;
		this.parser = parser;
		this.in = in;
		this.charset = charset;
		this.baseURI = baseURI;
	}

	@Override
	public boolean hasNext()
		throws QueryEvaluationException
	{
		return queue.hasNext();
	}

	@Override
	public Statement next()
		throws QueryEvaluationException
	{
		return queue.next();
	}

	@Override
	public void remove()
		throws QueryEvaluationException
	{
		queue.remove();
	}

	@Override
	protected void handleClose()
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
	public void run() {
		try {
			parser.setRDFHandler(this);
			if (charset == null) {
				parser.parse(in, baseURI);
			}
			else {
				parser.parse(new InputStreamReader(in, charset), baseURI);
			}
		}
		catch (RDFHandlerException e) {
			// parsing was cancelled or interrupted
		}
		catch (RDFParseException e) {
			queue.toss(e);
		}
		catch (IOException e) {
			queue.toss(e);
		}
		finally {
			queue.done();
		}
	}

	@Override
	public void startRDF()
		throws RDFHandlerException
	{
		// no-op
	}

	@Override
	public Map<String, String> getNamespaces() {
		try {
			namespacesReady.await();
			return namespaces;
		}
		catch (InterruptedException e) {
			throw new UndeclaredThrowableException(e);
		}
	}

	@Override
	public void handleComment(String comment)
		throws RDFHandlerException
	{
		// ignore
	}

	@Override
	public void handleNamespace(String prefix, String uri)
		throws RDFHandlerException
	{
		namespaces.put(prefix, uri);
	}

	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		namespacesReady.countDown();
		try {
			queue.put(st);
		}
		catch (InterruptedException e) {
			throw new RDFHandlerException(e);
		}
		if (closed)
			throw new RDFHandlerException("Result closed");
	}

	@Override
	public void endRDF()
		throws RDFHandlerException
	{
		namespacesReady.countDown();
	}

	@Override
	public void handleBaseURI(URI baseURI)
		throws RDFHandlerException
	{
			// ignore
	}

}
