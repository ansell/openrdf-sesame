/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage;

import org.openrdf.querymodel.Query;

/**
 * An interface for RDF query parsers that translate queries to OpenRDF query
 * moodels.
 */
public interface QueryParser {

	public Query parseQuery(String queryStr)
		throws MalformedQueryException;
}
