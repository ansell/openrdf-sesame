/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.sparqlxml;

import java.io.OutputStream;

import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.BooleanQueryResultWriterFactory;

/**
 * A {@link BooleanQueryResultWriterFactory} for writers of SPARQL/XML boolean
 * query results.
 * 
 * @author Arjohn Kampman
 */
public class SPARQLBooleanXMLWriterFactory implements BooleanQueryResultWriterFactory {

	/**
	 * Returns {@link BooleanQueryResultFormat#SPARQL}.
	 */
	public BooleanQueryResultFormat getFileFormat() {
		return BooleanQueryResultFormat.SPARQL;
	}

	/**
	 * Returns a new instance of {@link SPARQLBooleanXMLWriter}.
	 */
	public BooleanQueryResultWriter getWriter(OutputStream out) {
		return new SPARQLBooleanXMLWriter(out);
	}
}
