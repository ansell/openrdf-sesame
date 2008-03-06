/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sesame.webclient.query;

public class QueryInfo {

	private String _query;

	private String _queryLn;

	private String _repository;

	private String _resultFormat;

	public String getQueryLanguage() {
		return _queryLn;
	}

	public void setQueryLanguage(String queryLn) {
		_queryLn = queryLn;
	}

	public String getResultFormat() {
		return _resultFormat;
	}

	public void setResultFormat(String resultFormat) {
		_resultFormat = resultFormat;
	}

	public String getRepository() {
		return _repository;
	}

	public void setRepository(String repository) {
		_repository = repository;
	}

	/**
	 * @return Returns the query.
	 */
	public String getQuery() {
		return _query;
	}

	/**
	 * @param query
	 *        The query to set.
	 */
	public void setQuery(String query) {
		this._query = query;
	}

}
