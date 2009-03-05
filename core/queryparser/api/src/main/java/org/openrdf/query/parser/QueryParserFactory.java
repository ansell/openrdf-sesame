/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser;

import org.openrdf.query.QueryLanguage;

/**
 * A QueryParserFactory returns {@link QueryParser}s for a specific query
 * language.
 * 
 * @author Arjohn Kampman
 */
public interface QueryParserFactory {

	/**
	 * Returns the query language for this factory.
	 */
	public QueryLanguage getQueryLanguage();

	/**
	 * Returns a QueryParser instance.
	 */
	public QueryParser getParser();
}
