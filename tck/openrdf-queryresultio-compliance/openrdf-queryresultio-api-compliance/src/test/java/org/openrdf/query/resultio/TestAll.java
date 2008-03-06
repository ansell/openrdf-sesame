/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import org.openrdf.query.resultio.TupleQueryResultSerializationTest;
import org.openrdf.query.resultio.TupleQueryResultTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
 * @author Herko ter Horst
 */
public class TestAll extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.openrdf.query.resultio");
		//$JUnit-BEGIN$
		suite.addTestSuite(TupleQueryResultTest.class);
		suite.addTestSuite(TupleQueryResultSerializationTest.class);
		//$JUnit-END$
		return suite;
	}

}
