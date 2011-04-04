/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.io.IOException;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryException;

/**
 * TupleQuery specific to the HTTP protocol. Methods in this class may throw the
 * specific RepositoryException subclass UnautorizedException, the semantics of
 * which is defined by the HTTP protocol.
 * 
 * @see org.openrdf.http.protocol.UnauthorizedException
 * @author Arjohn Kampman
 */
public class HTTPBooleanQuery extends HTTPQuery implements BooleanQuery {

	public HTTPBooleanQuery(HTTPRepositoryConnection con, QueryLanguage ql, String queryString, String baseURI)
	{
		super(con, ql, queryString, baseURI);
	}

	public boolean evaluate()
		throws HTTPQueryEvaluationException
	{
		HTTPClient client = httpCon.getRepository().getHTTPClient();

		try {
			return client.sendBooleanQuery(queryLanguage, queryString, baseURI, dataset, includeInferred, maxQueryTime,
					getBindingsArray());
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
