/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.io.IOException;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;

/**
 * @author Arjohn Kampman
 */
class HTTPTupleQuery extends HTTPQuery implements TupleQuery {

	public HTTPTupleQuery(HTTPRepositoryConnection con, QueryLanguage ql, String queryString, String baseURI) {
		super(con, ql, queryString, baseURI);
	}

	public TupleQueryResult evaluate()
		throws QueryEvaluationException
	{
		HTTPClient client = _httpCon.getRepository().getHTTPClient();
		try {
			return client.sendTupleQuery(_queryLanguage, _queryString, _includeInferred);
		}
		catch (IOException e) {
			throw new QueryEvaluationException(e.getMessage(), e);
		}
	}

	public void evaluate(TupleQueryResultHandler handler)
		throws QueryEvaluationException, TupleQueryResultHandlerException
	{
		HTTPClient client = _httpCon.getRepository().getHTTPClient();
		try {
			client.sendTupleQuery(_queryLanguage, _queryString, _includeInferred, handler);
		}
		catch (IOException e) {
			throw new QueryEvaluationException(e.getMessage(), e);
		}
	}
}
