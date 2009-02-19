/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.ntriples;

import org.openrdf.rio.RDFParser;

/**
 * JUnit test for the N-Triples parser.
 * 
 * @author Arjohn Kampman
 */
public class NTriplesParserTest extends NTriplesParserTestCase {

	@Override
	protected RDFParser createRDFParser() {
		return new NTriplesParser();
	}
}
