/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.text.csv;

import java.io.OutputStream;

import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;

/**
 * A {@link TupleQueryResultWriterFactory} for writers of SPARQL/CSV tuple query
 * results.
 * 
 * @author Jeen Broekstra
 */
public class SPARQLResultsCSVWriterFactory implements TupleQueryResultWriterFactory {

	/**
	 * Returns {@link TupleQueryResultFormat#CSV}.
	 */
	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return TupleQueryResultFormat.CSV;
	}

	/**
	 * Returns a new instance of {@link SPARQLResultsCSVWriter}.
	 */
	public TupleQueryResultWriter getWriter(OutputStream out) {
		return new SPARQLResultsCSVWriter(out);
	}
}
