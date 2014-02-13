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
package org.openrdf.repository.http;

import java.io.IOException;

import org.openrdf.http.client.SparqlSession;
import org.openrdf.http.client.query.AbstractHTTPQuery;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryException;

/**
 * TupleQuery specific to the HTTP protocol. Methods in this class may throw the
 * specific RepositoryException subclass UnautorizedException, the semantics of
 * which is defined by the HTTP protocol.
 * 
 * @see org.openrdf.http.protocol.UnauthorizedException
 * @author Arjohn Kampman
 */
public class HTTPBooleanQuery extends AbstractHTTPQuery implements BooleanQuery {

	private final HTTPRepositoryConnection conn;

	public HTTPBooleanQuery(HTTPRepositoryConnection conn, QueryLanguage ql, String queryString, String baseURI)
	{
		super(conn.getSesameSession(), ql, queryString, baseURI);
		this.conn = conn;
	}

	public boolean evaluate()
		throws QueryEvaluationException
	{
		SparqlSession client = getHttpClient();

		try {
			conn.flushTransactionState(Protocol.Action.GET);
			return client.sendBooleanQuery(queryLanguage, queryString, baseURI, dataset, getIncludeInferred(), maxQueryTime,
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
}
