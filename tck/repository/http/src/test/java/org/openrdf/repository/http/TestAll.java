/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 
 * @author Herko ter Horst
 */
public class TestAll extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.openrdf.repository.http");
		// $JUnit-BEGIN$
		suite.addTestSuite(HTTPStoreConnectionTest.class);
		// $JUnit-END$
		return suite;
	}

}
