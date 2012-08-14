/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser;

import java.util.List;

import org.openrdf.query.MalformedQueryException;

/**
 * An RDF query parser translate query strings in some query language to OpenRDF
 * query models.
 */
public interface QueryParser {

	public ParsedUpdate parseUpdate(String updateStr, String baseURI)
		throws MalformedQueryException;

	public ParsedQuery parseQuery(String queryStr, String baseURI)
		throws MalformedQueryException;
}
