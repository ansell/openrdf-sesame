/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sparql.query;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.TupleQueryResultImpl;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLParser;

/**
 * Parses tuple results in the background.
 * 
 * @author James Leigh
 * 
 */
public class SPARQLTupleQuery extends SPARQLQuery implements TupleQuery {
	private SPARQLResultsXMLParser parser = new SPARQLResultsXMLParser();

	public SPARQLTupleQuery(HttpClient client, String url, String base,
			String query) {
		super(client, url, base, query);
	}

	public TupleQueryResult evaluate() throws QueryEvaluationException {
		try {
			BackgroundTupleResult result = null;
			HttpMethod response = getResponse();
			try {
				InputStream in = response.getResponseBodyAsStream();
				result = new BackgroundTupleResult(parser, in, response);
				execute(result);
				InsertBindingSetCursor cursor = new InsertBindingSetCursor(
						result, getBindings());
				List<String> list = new ArrayList<String>(
						result.getBindingNames());
				list.addAll(getBindingNames());
				return new TupleQueryResultImpl(list, cursor);
			} catch (HttpException e) {
				throw new QueryEvaluationException(e);
			} finally {
				if (result == null) {
					response.abort();
					response.releaseConnection();
				}
			}
		} catch (IOException e) {
			throw new QueryEvaluationException(e);
		}
	}

	public void evaluate(TupleQueryResultHandler handler)
			throws QueryEvaluationException, TupleQueryResultHandlerException {
		try {
			boolean complete = false;
			HttpMethod response = getResponse();
			try {
				parser.setTupleQueryResultHandler(handler);
				parser.parse(response.getResponseBodyAsStream());
				complete = true;
			} catch (HttpException e) {
				throw new QueryEvaluationException(e);
			} catch (QueryResultParseException e) {
				throw new QueryEvaluationException(e);
			} catch (TupleQueryResultHandlerException e) {
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
		return parser.getTupleQueryResultFormat().getDefaultMIMEType();
	}
}
