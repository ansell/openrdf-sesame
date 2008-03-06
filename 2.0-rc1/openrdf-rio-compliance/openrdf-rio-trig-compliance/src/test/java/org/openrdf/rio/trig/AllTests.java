/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.trig;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Arjohn Kampman
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.openrdf.rio.trig");
		// $JUnit-BEGIN$
		suite.addTestSuite(TriGParserTest.class);
		// $JUnit-END$
		return suite;
	}
}
