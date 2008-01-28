/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.turtle;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author akam
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.openrdf.rio.turtle");
		// $JUnit-BEGIN$
		suite.addTestSuite(TurtleParserTest.class);
		suite.addTestSuite(TurtleWriterTest.class);
		// $JUnit-END$
		return suite;
	}

}
