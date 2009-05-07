/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.trix;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;

/**
 * An {@link RDFParserFactory} for TriX parsers.
 * 
 * @author Arjohn Kampman
 */
public class TriXParserFactory implements RDFParserFactory {

	/**
	 * Returns {@link RDFFormat#TRIX}.
	 */
	public RDFFormat getRDFFormat() {
		return RDFFormat.TRIX;
	}

	/**
	 * Returns a new instance of {@link TriXParser}.
	 */
	public RDFParser getParser() {
		return new TriXParser();
	}
}
