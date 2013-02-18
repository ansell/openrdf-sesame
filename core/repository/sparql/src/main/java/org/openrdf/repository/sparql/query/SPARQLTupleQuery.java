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
package org.openrdf.repository.sparql.query;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLParser;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLConnection;

/**
 * Parses tuple results in the background.
 * 
 * @author James Leigh
 * 
 */
public class SPARQLTupleQuery extends SPARQLQuery implements TupleQuery {
	private SPARQLResultsXMLParser parser = new SPARQLResultsXMLParser();

	// TODO move to base class
	protected final SPARQLConnection conn;
	protected final String baseUri;
	
	public SPARQLTupleQuery(SPARQLConnection conn, HttpClient client, String url, String baseUri,
			String queryString) {
		super(client, url, baseUri, queryString);
		this.conn = conn;
		this.baseUri = baseUri;
	}
	
	// TODO move to base class, share with HTTPTupleQuery
	// maybe change signature of HTTPClient to take BindingSet instead of Binding...
	protected Binding[] getBindingsArray() {
		BindingSet bindings = this.getBindings();

		Binding[] bindingsArray = new Binding[bindings.size()];

		Iterator<Binding> iter = bindings.iterator();
		for (int i = 0; i < bindings.size(); i++) {
			bindingsArray[i] = iter.next();
		}

		return bindingsArray;
	}

	public TupleQueryResult evaluate() throws QueryEvaluationException {
		
		HTTPClient client = conn.getRepository().getNewHttpClient();
		try {
			// TODO getQueryString() already inserts bindings
			return client.sendTupleQuery(QueryLanguage.SPARQL, getQueryString(), baseUri, dataset, false, maxQueryTime, getBindingsArray());
		} 
		catch (IOException e) {
			throw new QueryEvaluationException(e.getMessage(), e);
		}
		catch (RepositoryException e) {
			throw new QueryEvaluationException(e.getMessage(), e);
		}
		catch (MalformedQueryException e) {
			throw new QueryEvaluationException(e.getMessage(), e);
		}
	}

	public void evaluate(TupleQueryResultHandler handler)
			throws QueryEvaluationException, TupleQueryResultHandlerException {
		try {
			boolean complete = false;
			HttpMethod response = getResponse();
			try {
				parser.setQueryResultHandler(handler);
				parser.parseQueryResult(response.getResponseBodyAsStream());
				complete = true;
			} catch (HttpException e) {
				throw new QueryEvaluationException(e);
			} catch (QueryResultParseException e) {
				throw new QueryEvaluationException(e);
			} catch (QueryResultHandlerException e) {
				throw new QueryEvaluationException(e);
			} finally {
				if (!complete) {
					response.abort();
				}
			}
		} catch (IOException e) {
			throw new QueryEvaluationException(e);
		}
	}

	@Override
	protected String getAccept() {
		return parser.getQueryResultFormat().getDefaultMIMEType();
	}
}
