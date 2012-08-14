/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserFactory;

/**
 * A {@link QueryParserFactory} for SeRQL parsers
 * 
 * @author Arjohn Kampman
 */
public class SeRQLParserFactory implements QueryParserFactory {

	private final SeRQLParser singleton = new SeRQLParser();

	/**
	 * Returns {@link QueryLanguage#SERQL}.
	 */
	public QueryLanguage getQueryLanguage() {
		return QueryLanguage.SERQL;
	}

	/**
	 * Returns a shared, thread-safe, instance of SeRQLParser.
	 */
	public QueryParser getParser() {
		return singleton;
	}
}
