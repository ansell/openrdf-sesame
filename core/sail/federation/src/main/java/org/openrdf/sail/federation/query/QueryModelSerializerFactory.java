/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.query;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserFactory;

/**
 * Create a {@link QueryModelSerializer}.
 * 
 * @author James Leigh
 */
public class QueryModelSerializerFactory implements QueryParserFactory {

	public QueryParser getParser() {
		return new QueryModelSerializer();
	}

	public QueryLanguage getQueryLanguage() {
		return QueryModelSerializer.LANGUAGE;
	}

}
