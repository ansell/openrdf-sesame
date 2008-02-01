/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
 * @author Herko ter Horst
 */
public class TestAll extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.openrdf.sail.memory");
		//$JUnit-BEGIN$
		suite.addTestSuite(MemoryStoreTest.class);
		suite.addTestSuite(StoreSerializationTest.class);
		//$JUnit-END$
		return suite;
	}

}
