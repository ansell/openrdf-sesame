/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
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
