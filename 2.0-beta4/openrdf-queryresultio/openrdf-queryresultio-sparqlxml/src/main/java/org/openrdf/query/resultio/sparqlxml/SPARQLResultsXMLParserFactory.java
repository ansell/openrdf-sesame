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
 * A {@link QueryResultParserFactory} for SeRQL parsers
 * 
 * @author Arjohn Kampman
 */
public class SPARQLResultsXMLParserFactory implements TupleQueryResultParserFactory {

	/**
	 * Returns {@link TupleQueryResultFormat#SPARQL}.
	 */
	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return TupleQueryResultFormat.SPARQL;
	}

	/**
	 * Returns a new instance of SPARQLResultsXMLParser.
	 */
	public TupleQueryResultParser getParser() {
		return new SPARQLResultsXMLParser();
	}
}
