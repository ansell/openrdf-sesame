/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.sparqlxml;

import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultParser;
import org.openrdf.query.resultio.BooleanQueryResultParserFactory;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultParserFactory;

/**
 * A {@link TupleQueryResultParserFactory} for parsers of SPARQL/XML boolean
 * query results.
 * 
 * @author Arjohn Kampman
 */
public class SPARQLBooleanXMLParserFactory implements BooleanQueryResultParserFactory {

	/**
	 * Returns {@link TupleQueryResultFormat#SPARQL}.
	 */
	public BooleanQueryResultFormat getFileFormat() {
		return BooleanQueryResultFormat.SPARQL;
	}

	/**
	 * Returns a new instance of SPARQLBooleanXMLParser.
	 */
	public BooleanQueryResultParser getParser() {
		return new SPARQLBooleanXMLParser();
	}
}
