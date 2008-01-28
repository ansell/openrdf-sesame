/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.util;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestAll {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.openrdf.model.util");
		// $JUnit-BEGIN$
		suite.addTestSuite(ModelEqualityTest.class);
		// $JUnit-END$
		return suite;
	}
}
