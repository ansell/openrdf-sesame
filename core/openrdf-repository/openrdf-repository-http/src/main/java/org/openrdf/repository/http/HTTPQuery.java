/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.impl.AbstractQuery;

/**
 * @author Arjohn Kampman
 */
class HTTPQuery extends AbstractQuery {

	protected HTTPRepositoryConnection _httpCon;

	protected QueryLanguage _queryLanguage;

	protected String _queryString;
	
	protected String _baseURI;

	public HTTPQuery(HTTPRepositoryConnection con, QueryLanguage ql, String queryString, String baseURI) {
		_httpCon = con;
		_queryLanguage = ql;
		_queryString = queryString;
		_baseURI = baseURI;
	}
}
