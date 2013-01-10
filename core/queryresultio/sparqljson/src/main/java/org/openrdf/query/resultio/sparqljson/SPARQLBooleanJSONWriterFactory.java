/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.sparqljson;

import java.io.OutputStream;

import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.BooleanQueryResultWriterFactory;

/**
 * A {@link BooleanQueryResultWriterFactory} for writers of SPARQL/JSON query
 * boolean results.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class SPARQLBooleanJSONWriterFactory implements BooleanQueryResultWriterFactory {

	/**
	 * Returns {@link BooleanQueryResultFormat#JSON}.
	 */
	public BooleanQueryResultFormat getBooleanQueryResultFormat() {
		return BooleanQueryResultFormat.JSON;
	}

	/**
	 * Returns a new instance of SPARQLResultsJSONWriter.
	 */
	public BooleanQueryResultWriter getWriter(OutputStream out) {
		return new SPARQLBooleanJSONWriter(out);
	}
}
