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
package org.eclipse.rdf4j.repository.http;

import java.io.IOException;

import org.eclipse.rdf4j.http.client.SparqlSession;
import org.eclipse.rdf4j.http.client.query.AbstractHTTPQuery;
import org.eclipse.rdf4j.http.protocol.Protocol;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;

/**
 * GraphQuery implementation specific to the HTTP protocol. Methods in this
 * class may throw the specific RepositoryException subclass
 * UnautorizedException, the semantics of which is defined by the HTTP protocol.
 * 
 * @see org.eclipse.rdf4j.http.protocol.UnauthorizedException
 * @author Arjohn Kampman
 * @author Herko ter Horst
 * @author Andreas Schwarte
 */
public class HTTPGraphQuery extends AbstractHTTPQuery implements GraphQuery {

	private final HTTPRepositoryConnection conn;

	public HTTPGraphQuery(HTTPRepositoryConnection conn, QueryLanguage ql, String queryString, String baseURI)
	{
		super(conn.getSesameSession(), ql, queryString, baseURI);
		this.conn = conn;
	}

	public GraphQueryResult evaluate()
		throws QueryEvaluationException
	{
		SparqlSession client = getHttpClient();
		try {
			conn.flushTransactionState(Protocol.Action.QUERY);
			return client.sendGraphQuery(queryLanguage, queryString, baseURI, dataset, getIncludeInferred(),
					getMaxExecutionTime(), getBindingsArray());
		}
		catch (IOException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		}
		catch (RepositoryException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		}
		catch (MalformedQueryException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		}
	}

	/*
	public GraphQueryResult evaluate()
		throws QueryEvaluationException
	{
		HTTPClient client = httpCon.getRepository().getHTTPClient();

		
		try {
			return client.sendGraphQuery(queryLanguage, queryString, baseURI, dataset, includeInferred, maxQueryTime,
					getBindingsArray());
		}
		catch (IOException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		}
		catch (RepositoryException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		}
		catch (MalformedQueryException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		}
	}
	*/

	public void evaluate(RDFHandler handler)
		throws QueryEvaluationException, RDFHandlerException
	{
		SparqlSession client = getHttpClient();
		try {
			conn.flushTransactionState(Protocol.Action.QUERY);
			client.sendGraphQuery(queryLanguage, queryString, baseURI, dataset, includeInferred,
					getMaxExecutionTime(), handler, getBindingsArray());
		}
		catch (IOException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		}
		catch (RepositoryException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		}
		catch (MalformedQueryException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		}
	}
}
