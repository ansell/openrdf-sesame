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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.sparqlxml.SPARQLBooleanXMLParser;

/**
 * Parses boolean query response from remote stores.
 * 
 * @author James Leigh
 * 
 */
public class SPARQLBooleanQuery extends SPARQLQuery implements BooleanQuery {
	private SPARQLBooleanXMLParser parser = new SPARQLBooleanXMLParser();

	public SPARQLBooleanQuery(HttpClient client, String url, String base,
			String query) {
		super(client, url, base, query);
	}

	public boolean evaluate() throws QueryEvaluationException {
		try {
			boolean complete = false;
			HttpMethod response = getResponse();
			try {
				boolean result = parser.parse(response
						.getResponseBodyAsStream());
				response.releaseConnection();
				complete = true;
				return result;
			} catch (HttpException e) {
				throw new QueryEvaluationException(e);
			} catch (QueryResultParseException e) {
				throw new QueryEvaluationException(e);
			} finally {
				if (!complete) {
					response.abort();
					response.releaseConnection();
				}
			}
		} catch (IOException e) {
			throw new QueryEvaluationException(e);
		}
	}

	@Override
	protected String getAccept() {
		return parser.getBooleanQueryResultFormat().getDefaultMIMEType();
	}
}
