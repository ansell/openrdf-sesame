/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.binary;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;

/**
 * An {@link RDFParserFactory} for Binary RDF parsers.
 * 
 * @author Arjohn Kampman
 */
public class BinaryRDFParserFactory implements RDFParserFactory {

	/**
	 * Returns {@link RDFFormat#BINARY}.
	 */
	public RDFFormat getRDFFormat() {
		return RDFFormat.BINARY;
	}

	/**
	 * Returns a new instance of {@link BinaryRDFParser}.
	 */
	public RDFParser getParser() {
		return new BinaryRDFParser();
	}
}
