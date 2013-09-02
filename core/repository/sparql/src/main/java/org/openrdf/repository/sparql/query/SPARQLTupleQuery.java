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

import org.openrdf.http.client.HTTPClient;
import org.openrdf.http.client.query.AbstractHTTPQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;

/**
 * Parses tuple results in the background.
 * 
 * @author James Leigh
 */
public class SPARQLTupleQuery extends AbstractHTTPQuery implements TupleQuery {

	// TODO there was some magic going on in SparqlOperation to get baseURI
	// directly replaced within the query using BASE

	public SPARQLTupleQuery(HTTPClient httpClient, String baseUri, String queryString) {
		super(httpClient, QueryLanguage.SPARQL, queryString, baseUri);
	}

	public TupleQueryResult evaluate()
		throws QueryEvaluationException
	{

		HTTPClient client = getHttpClient();
		try {
			return client.sendTupleQuery(QueryLanguage.SPARQL, getQueryString(), baseURI, dataset, getIncludeInferred(),
					maxQueryTime, getBindingsArray());
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
		throws QueryEvaluationException, TupleQueryResultHandlerException
	{

		HTTPClient client = getHttpClient();
		try {
			client.sendTupleQuery(QueryLanguage.SPARQL, getQueryString(), baseURI, dataset, getIncludeInferred(), maxQueryTime,
					handler, getBindingsArray());
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

	private String getQueryString() {
		return QueryStringUtil.getQueryString(queryString, getBindings());
	}
}
