/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.sparqljson;

import java.io.OutputStream;

import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;

/**
 * A {@link TupleQueryResultWriterFactory} for writers of SPARQL/JSON query
 * results.
 * 
 * @author Arjohn Kampman
 */
public class SPARQLResultsJSONWriterFactory implements TupleQueryResultWriterFactory {

	/**
	 * Returns {@link TupleQueryResultFormat#JSON}.
	 */
	public TupleQueryResultFormat getFileFormat() {
		return TupleQueryResultFormat.JSON;
	}

	/**
	 * Returns a new instance of SPARQLResultsJSONWriter.
	 */
	public TupleQueryResultWriter getWriter(OutputStream out) {
		return new SPARQLResultsJSONWriter(out);
	}
}
