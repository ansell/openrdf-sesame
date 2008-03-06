/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser;

import org.openrdf.query.MalformedQueryException;


/**
 * An interface for RDF query parsers that translate queries to OpenRDF query
 * moodels.
 */
public interface QueryParser {

	public ParsedQuery parseQuery(String queryStr, String baseURI)
		throws MalformedQueryException;
}
