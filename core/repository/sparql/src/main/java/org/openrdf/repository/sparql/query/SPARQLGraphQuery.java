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

import org.openrdf.http.client.SparqlSession;
import org.openrdf.http.client.query.AbstractHTTPQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * Parses RDF results in the background.
 * 
 * @author James Leigh
 * @author Andreas Schwarte
 * 
 */
public class SPARQLGraphQuery extends AbstractHTTPQuery implements GraphQuery {

	public SPARQLGraphQuery(SparqlSession httpClient, String baseURI,
			String queryString) {
		super(httpClient, QueryLanguage.SPARQL, queryString, baseURI);
	}

	public GraphQueryResult evaluate() throws QueryEvaluationException {
		SparqlSession client = getHttpClient();
		try {
			// TODO getQueryString() already inserts bindings, use emptybindingset as last argument?
			return client.sendGraphQuery(queryLanguage, getQueryString(), baseURI, dataset, getIncludeInferred(), maxQueryTime, getBindingsArray());
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

	public void evaluate(RDFHandler handler) throws QueryEvaluationException,
			RDFHandlerException {
		
		SparqlSession client = getHttpClient();
		try {
			client.sendGraphQuery(queryLanguage, getQueryString(), baseURI, dataset, getIncludeInferred(), maxQueryTime, handler,
					getBindingsArray());
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
