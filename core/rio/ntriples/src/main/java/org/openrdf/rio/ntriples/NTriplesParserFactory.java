/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.ntriples;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;

/**
 * An {@link RDFParserFactory} for N-Triples parsers.
 * 
 * @author Arjohn Kampman
 */
public class NTriplesParserFactory implements RDFParserFactory {

	/**
	 * Returns {@link RDFFormat#NTRIPLES}.
	 */
	public RDFFormat getFileFormat() {
		return RDFFormat.NTRIPLES;
	}

	/**
	 * Returns a new instance of NTriplesParser.
	 */
	public RDFParser getParser() {
		return new NTriplesParser();
	}
}
