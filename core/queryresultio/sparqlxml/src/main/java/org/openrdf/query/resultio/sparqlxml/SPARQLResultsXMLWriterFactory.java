/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.sparqlxml;

import java.io.OutputStream;

import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;

/**
 * A {@link TupleQueryResultWriterFactory} for writers of SPARQL/XML tuple query
 * results.
 * 
 * @author Arjohn Kampman
 */
public class SPARQLResultsXMLWriterFactory implements TupleQueryResultWriterFactory {

	/**
	 * Returns {@link TupleQueryResultFormat#SPARQL}.
	 */
	public TupleQueryResultFormat getFileFormat() {
		return TupleQueryResultFormat.SPARQL;
	}

	/**
	 * Returns a new instance of {@link SPARQLResultsXMLWriter}.
	 */
	public TupleQueryResultWriter getWriter(OutputStream out) {
		return new SPARQLResultsXMLWriter(out);
	}
}
