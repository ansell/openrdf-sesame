/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.trig;

import junit.framework.Test;

import org.openrdf.rio.RDFParser;

/**
 * JUnit test for the TriG parser.
 */
public class TriGParserTest extends TriGParserTestCase {

	public static Test suite()
		throws Exception
	{
		return new TriGParserTest().createTestSuite();
	}

	@Override
	protected RDFParser createRDFParser() {
		return new TriGParser();
	}

}
