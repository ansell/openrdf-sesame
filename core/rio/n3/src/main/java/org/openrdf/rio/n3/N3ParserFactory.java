/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.n3;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.turtle.TurtleParser;

/**
 * An {@link RDFParserFactory} for N3 parsers.
 * 
 * @author Arjohn Kampman
 */
public class N3ParserFactory implements RDFParserFactory {

	/**
	 * Returns {@link RDFFormat#N3}.
	 */
	public RDFFormat getFileFormat() {
		return RDFFormat.N3;
	}

	/**
	 * Returns a new instance of {@link TurtleParser}.
	 */
	public RDFParser getParser() {
		return new TurtleParser();
	}
}
