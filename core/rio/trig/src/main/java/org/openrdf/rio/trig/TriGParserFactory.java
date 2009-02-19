/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.trig;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;

/**
 * An {@link RDFParserFactory} for TriG parsers.
 * 
 * @author Arjohn Kampman
 */
public class TriGParserFactory implements RDFParserFactory {

	/**
	 * Returns {@link RDFFormat#TRIG}.
	 */
	public RDFFormat getRDFFormat() {
		return RDFFormat.TRIG;
	}

	/**
	 * Returns a new instance of {@link TriGParser}.
	 */
	public RDFParser getParser() {
		return new TriGParser();
	}
}
