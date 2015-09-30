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
package org.openrdf.repository.sparql.query;

import java.io.IOException;

import org.openrdf.http.client.SparqlSession;
import org.openrdf.http.client.query.AbstractHTTPQuery;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryException;

/**
 * Parses boolean query response from remote stores.
 * 
 * @author James Leigh
 * 
 */
public class SPARQLBooleanQuery extends AbstractHTTPQuery implements BooleanQuery {

	public SPARQLBooleanQuery(SparqlSession httpClient, String baseURI,
			String queryString) {
		super(httpClient, QueryLanguage.SPARQL, queryString, baseURI);
	}

	public boolean evaluate() throws QueryEvaluationException {
		
		SparqlSession client = getHttpClient();

		try {
			return client.sendBooleanQuery(queryLanguage, getQueryString(), baseURI, dataset, getIncludeInferred(), getMaxExecutionTime(),
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
