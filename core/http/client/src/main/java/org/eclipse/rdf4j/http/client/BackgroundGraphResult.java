/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.http.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.eclipse.rdf4j.common.iteration.IterationWrapper;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;

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

	public BackgroundGraphResult(RDFParser parser, InputStream in, Charset charset, String baseURI) {
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
		try {
			super.handleClose();
		}
		finally {
			closed = true;
			try {
				in.close();
			}
			catch (IOException e) {
				throw new QueryEvaluationException(e);
			}
			finally {
				queue.close();
			}
		}
	}

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
			namespacesReady.countDown();
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
		try {
			queue.put(st);
		}
		catch (InterruptedException e) {
			throw new RDFHandlerException(e);
		}
		if (closed)
			throw new RDFHandlerException("Result closed");
	}

	public void endRDF()
		throws RDFHandlerException
	{
		namespacesReady.countDown();
	}

}