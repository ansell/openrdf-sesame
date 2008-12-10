/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import org.openrdf.http.client.RepositoryClient;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.store.StoreException;

/**
 * GraphQuery implementation specific to the HTTP protocol.
 * 
 * Methods in this class may throw the specific StoreException subclasses
 * UnautorizedException and NotAllowedException, the semantics of which are
 * defined by the HTTP protocol.
 * 
 * @see org.openrdf.http.protocol.UnauthorizedException
 * @see org.openrdf.http.protocol.NotAllowedException
 * 
 * @author Arjohn Kampman
 * @author Herko ter Horst
 */
public class HTTPGraphQuery extends HTTPQuery implements GraphQuery {

	public HTTPGraphQuery(HTTPRepositoryConnection con, QueryLanguage ql, String queryString, String baseURI) {
		super(con, ql, queryString, baseURI);
	}

	public GraphQueryResult evaluate()
		throws HTTPQueryEvaluationException
	{
		RepositoryClient client = httpCon.getClient();

		try {
			return client.sendGraphQuery(queryLanguage, queryString, dataset, includeInferred, getBindingsArray());
		}
		catch (StoreException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		}
		catch (MalformedQueryException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		}
	}

	public void evaluate(RDFHandler handler)
		throws HTTPQueryEvaluationException, RDFHandlerException
	{
		RepositoryClient client = httpCon.getClient();
		try {
			client.sendGraphQuery(queryLanguage, queryString, dataset, includeInferred, handler, getBindingsArray());
		}
		catch (StoreException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		}
		catch (MalformedQueryException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		}
	}
}
