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
