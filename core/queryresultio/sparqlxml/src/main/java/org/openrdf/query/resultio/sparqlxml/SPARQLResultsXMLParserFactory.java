/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.sparqlxml;

import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultParser;
import org.openrdf.query.resultio.TupleQueryResultParserFactory;

/**
 * A {@link TupleQueryResultParserFactory} for parsers of SPARQL/XML tuple query
 * results.
 * 
 * @author Arjohn Kampman
 */
public class SPARQLResultsXMLParserFactory implements TupleQueryResultParserFactory {

	/**
	 * Returns {@link TupleQueryResultFormat#SPARQL}.
	 */
	public TupleQueryResultFormat getFileFormat() {
		return TupleQueryResultFormat.SPARQL;
	}

	/**
	 * Returns a new instance of {@link SPARQLResultsXMLParser}.
	 */
	public TupleQueryResultParser getParser() {
		return new SPARQLResultsXMLParser();
	}
}
