/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.turtle;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;

/**
 * An {@link RDFParserFactory} for Turtle parsers.
 * 
 * @author Arjohn Kampman
 */
public class TurtleParserFactory implements RDFParserFactory {

	/**
	 * Returns {@link RDFFormat#TURTLE}.
	 */
	public RDFFormat getRDFFormat() {
		return RDFFormat.TURTLE;
	}

	/**
	 * Returns a new instance of {@link TurtleParser}.
	 */
	public RDFParser getParser() {
		return new TurtleParser();
	}
}
