/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.n3;

import junit.framework.Test;

import org.openrdf.rio.RDFParser;
import org.openrdf.rio.turtle.TurtleParser;

/**
 * JUnit test for the N3 parser that uses the tests that are available <a
 * href="http://www.w3.org/2000/10/swap/test/n3parser.tests">online</a>.
 */
public class N3ParserTest extends N3ParserTestCase {

	public static Test suite()
		throws Exception
	{
		return new N3ParserTest().createTestSuite();
	}

	@Override
	protected RDFParser createRDFParser() {
		return new TurtleParser();
	}
}
