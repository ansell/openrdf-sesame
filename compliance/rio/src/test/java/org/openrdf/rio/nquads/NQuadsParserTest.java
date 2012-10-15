/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.nquads;

import org.openrdf.rio.RDFParser;

/**
 * JUnit test for the N-Quads parser.
 * 
 * @author Peter Ansell
 */
public class NQuadsParserTest extends NQuadsParserTestCase {

	@Override
	protected RDFParser createRDFParser() {
		return new NQuadsParser();
	}
}
