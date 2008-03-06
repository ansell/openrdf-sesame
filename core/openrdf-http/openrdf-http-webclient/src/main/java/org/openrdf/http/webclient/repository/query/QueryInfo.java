/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.query;

import org.openrdf.query.QueryLanguage;

public class QueryInfo {

	private String _query;

	private QueryLanguage _queryLn;

	private boolean _includeInferred;

	public String getQuery() {
		return _query;
	}

	public void setQuery(String query) {
		this._query = query;
	}

	public QueryLanguage getQueryLanguage() {
		return _queryLn;
	}

	public void setQueryLanguage(QueryLanguage queryLn) {
		_queryLn = queryLn;
	}

	public boolean isIncludeInferred() {
		return _includeInferred;
	}

	public void setIncludeInferred(boolean includeInferred) {
		_includeInferred = includeInferred;
	}
}
