/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.turtle;

import junit.framework.Test;

import org.openrdf.rio.RDFParser;

/**
 * JUnit test for the Turtle parser that uses the tests that are available <a
 * href="https://github.com/dajobe/raptor/commits/master/tests/turtle/">online</a>.
 */
public class TurtleParserTest extends TurtleParserTestCase {

	public static Test suite()
		throws Exception
	{
		return new TurtleParserTest().createTestSuite();
	}

	@Override
	protected RDFParser createRDFParser() {
		return new TurtleParser();
	}
}
