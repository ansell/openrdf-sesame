/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 
 * @author James Leigh
 */
public class TestAll extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.openrdf.repository");
		// $JUnit-BEGIN$
		suite.addTestSuite(PgSqlStoreConnectionTest.class);
		suite.addTestSuite(MySqlStoreConnectionTest.class);
		// $JUnit-END$
		return suite;
	}

}
