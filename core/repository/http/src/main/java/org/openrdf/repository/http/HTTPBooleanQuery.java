/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import org.openrdf.http.client.RepositoryClient;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.store.StoreException;

/**
 * TupleQuery specific to the HTTP protocol. Methods in this class may throw the
 * specific StoreException subclasses UnautorizedException and
 * NotAllowedException, the semantics of which are defined by the HTTP protocol.
 * 
 * @see org.openrdf.http.protocol.UnauthorizedException
 * @see org.openrdf.http.protocol.NotAllowedException
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
		RepositoryClient client = httpCon.getClient();

		try {
			return client.sendBooleanQuery(queryLanguage, queryString, dataset, includeInferred, getBindingsArray());
		}
		catch (StoreException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		}
		catch (MalformedQueryException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		}
	}
}
