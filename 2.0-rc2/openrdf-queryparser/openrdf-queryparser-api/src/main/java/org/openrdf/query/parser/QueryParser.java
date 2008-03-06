/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser;

import org.openrdf.query.MalformedQueryException;

/**
 * An RDF query parser translate query strings in some query language to OpenRDF
 * query models.
 */
public interface QueryParser {

	public ParsedQuery parseQuery(String queryStr, String baseURI)
		throws MalformedQueryException;
}
