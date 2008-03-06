/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.io.IOException;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * @author Arjohn Kampman
 */
public class HTTPGraphQuery extends HTTPQuery implements GraphQuery {

	public HTTPGraphQuery(HTTPRepositoryConnection con, QueryLanguage ql, String queryString, String baseURI) {
		super(con, ql, queryString, baseURI);
	}

	public GraphQueryResult evaluate()
		throws HTTPQueryEvaluationException
	{
		HTTPClient client = _httpCon.getRepository().getHTTPClient();

		try {
			return client.sendGraphQuery(_queryLanguage, _queryString, _includeInferred, getBindingsArray());
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

	public void evaluate(RDFHandler handler)
		throws HTTPQueryEvaluationException, RDFHandlerException
	{
		HTTPClient client = _httpCon.getRepository().getHTTPClient();
		try {
			client.sendGraphQuery(_queryLanguage, _queryString, _includeInferred, handler);
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
