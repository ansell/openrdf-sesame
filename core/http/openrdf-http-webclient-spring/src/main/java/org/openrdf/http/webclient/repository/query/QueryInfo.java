/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.query;

import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;

public class QueryInfo {

	private String queryString;

	private QueryLanguage queryLanguage;

	private boolean includeInferred;
	
	private Query query;

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String query) {
		this.queryString = query;
	}

	public QueryLanguage getQueryLanguage() {
		return queryLanguage;
	}

	public void setQueryLanguage(QueryLanguage queryLn) {
		this.queryLanguage = queryLn;
	}

	public boolean isIncludeInferred() {
		return includeInferred;
	}

	public void setIncludeInferred(boolean includeInferred) {
		this.includeInferred = includeInferred;
	}
	
	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
	}
}
